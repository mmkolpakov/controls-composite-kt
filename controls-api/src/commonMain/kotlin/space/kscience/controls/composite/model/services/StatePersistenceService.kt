package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.device.StatefulDevice
import space.kscience.controls.composite.model.state.StateSnapshot
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.*

/**
 * A service responsible for snapshotting and restoring the state of a [StatefulDevice].
 * This decouples the persistence mechanism from the device's business logic.
 */
@OptIn(InternalControlsApi::class)
public interface StatePersistenceService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Creates a snapshot of the device's current logical state.
     */
    public suspend fun snapshot(device: StatefulDevice): StateSnapshot {
        val stateMeta = MutableMeta()
        device.statefulProperties.forEach { element ->
            element.snapshotValue()?.let { metaValue ->
                stateMeta[element.propertyName] = metaValue
            }
        }
        return StateSnapshot(
            version = device.dirtyVersion.value,
            schemaVersion = device.blueprint.declaration.schemaVersion,
            state = stateMeta
        )
    }

    /**
     * Restores the device's state from a given snapshot.
     *
     * @throws IllegalStateException if the schema versions do not match and no `StateMigrator` is available.
     */
    public suspend fun restore(device: StatefulDevice, snapshot: StateSnapshot) {
        val currentSchemaVersion = device.blueprint.declaration.schemaVersion
        val restoredState = if (snapshot.schemaVersion < currentSchemaVersion) {
            val migratorId = device.blueprint.declaration.stateMigratorId
                ?: error("Snapshot schema version mismatch (snapshot: ${snapshot.schemaVersion}, device: $currentSchemaVersion) and no 'stateMigratorId' defined.")
            val migratorRegistry = device.context.request(StateMigratorRegistry)
            val migrator = migratorRegistry.find(migratorId, snapshot.schemaVersion, currentSchemaVersion)
                ?: error("State migrator with id '$migratorId' for versions ${snapshot.schemaVersion} -> $currentSchemaVersion not found.")
            migrator.migrate(snapshot)
        } else if (snapshot.schemaVersion > currentSchemaVersion) {
            error("Snapshot schema version ${snapshot.schemaVersion} is newer than device version $currentSchemaVersion.")
        } else {
            snapshot
        }

        val propertyMap = device.statefulProperties.associateBy { it.propertyName }
        val missingProperties = mutableListOf<String>()

        restoredState.state.items.forEach { (token, metaItem) ->
            val propertyName = token.toString()
            val element = propertyMap[propertyName]

            if (element != null) {
                try {
                    element.restoreValue(metaItem)
                } catch (e: Exception) {
                    device.context.logger.error { "Failed to restore property '$propertyName': ${e.message}" }
                }
            } else {
                missingProperties.add(propertyName)
            }
        }

        if (missingProperties.isNotEmpty()) {
            device.context.logger.warn {
                "The following properties were found in the snapshot but are not registered in the device '$device': $missingProperties. They were ignored."
            }
        }

        device.clearDirtyFlag(device.dirtyVersion.value)
    }

    public companion object : PluginFactory<StatePersistenceService> {
        override val tag: PluginTag = PluginTag("device.state.persistence", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): StatePersistenceService {
            error("StatePersistenceService is a service interface and requires a runtime-specific implementation.")
        }
    }
}
