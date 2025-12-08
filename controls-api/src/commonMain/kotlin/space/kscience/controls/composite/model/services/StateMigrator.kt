package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.state.StateSnapshot
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

//TODO move interface contract from here
/**
 * A contract for a service that can migrate a [StateSnapshot] from one schema version to another.
 * This is a critical component for ensuring long-term state compatibility as device blueprints evolve.
 *
 * Implementations of this interface are expected to be stateless, reusable, and represent a pure
 * function that transforms state data without side effects.
 */
public interface StateMigrator {
    /**
     * The unique identifier for this migrator logic. This ID is referenced by a `DeviceBlueprintDeclaration`.
     */
    public val id: String

    /**
     * The schema version this migrator converts **from**.
     */
    public val fromVersion: Int

    /**
     * The schema version this migrator converts **to**.
     */
    public val toVersion: Int

    /**
     * Performs the state migration.
     *
     * @param oldState The [StateSnapshot] in the `fromVersion` schema.
     * @return A new [StateSnapshot] in the `toVersion` schema.
     * @throws Exception if the migration logic fails. The `hotSwap` operation will be rolled back.
     */
    public suspend fun migrate(oldState: StateSnapshot): StateSnapshot
}

/**
 * A contract for a service that discovers and provides [StateMigrator] instances.
 * The runtime uses this registry to find the appropriate migrator (or a chain of migrators)
 * when a device's state needs to be upgraded from an older snapshot during a `hotSwap` operation.
 */
public interface StateMigratorRegistry : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Finds a [StateMigrator] by its unique identifier and target version.
     *
     * @param id The unique ID of the migrator logic.
     * @param from The source schema version.
     * @param to The target schema version.
     * @return The found [StateMigrator] instance, or `null` if no matching migrator is found.
     */
    public fun find(id: String, from: Int, to: Int): StateMigrator?

    /**
     * Provides a collection of all known migrators in this registry.
     * This is useful for introspection and for building migration paths.
     */
    public fun getAll(): Collection<StateMigrator>

    public companion object : PluginFactory<StateMigratorRegistry> {
        override val tag: PluginTag = PluginTag("device.state.migrator.registry", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime module.
         */
        override fun build(context: Context, meta: Meta): StateMigratorRegistry {
            error("StateMigratorRegistry is a service interface and requires a runtime-specific implementation.")
        }
    }
}
