package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

/**
 * A DataForge plugin that provides a [SnapshotStore] for device state persistence.
 * The type of store is configured via the plugin's metadata and is discovered dynamically
 * using the [SnapshotStoreFactory] plugin mechanism.
 *
 * Example configuration:
 * ```
 * plugin(PersistencePlugin) {
 *     "store" {
 *         "type" put "file" // or "memory", "localStorage"
 *         "path" put "./snapshots" // for "file" store
 *     }
 * }
 * ```
 */
public class PersistencePlugin(meta: Meta) : AbstractPlugin(meta) {
    override val tag: PluginTag get() = Companion.tag

    private val factories by lazy {
        context.gather<SnapshotStoreFactory>(SnapshotStoreFactory.SNAPSHOT_STORE_FACTORY_TARGET)
    }

    /**
     * Creates a new [SnapshotStore] instance based on the provided metadata.
     * The 'type' property in the meta is used to find the appropriate factory.
     * @param meta The configuration for the store.
     * @return A new [SnapshotStore] instance.
     * @throws IllegalStateException if a factory for the specified type is not found.
     */
    public fun newStore(meta: Meta): SnapshotStore {
        val type = meta["type"].string ?: "memory" // Default to in-memory if not specified
        val factory = factories.values.find { it.type == type }
            ?: error("A snapshot store factory for type '$type' is not registered in the context.")
        return factory.build(context, meta)
    }

    /**
     * The default snapshot store instance, configured via the plugin's metadata block.
     * If no configuration is provided, it defaults to an in-memory store.
     */
    public val store: SnapshotStore by lazy {
        newStore(meta["store"] ?: Meta.EMPTY)
    }

    override fun content(target: String): Map<Name, Any> = when (target) {
        SnapshotStoreFactory.SNAPSHOT_STORE_FACTORY_TARGET -> mapOf(
            InMemorySnapshotStore.type.asName() to InMemorySnapshotStore,
            FileSnapshotStore.type.asName() to FileSnapshotStore
        ) + platformSnapshotStoreFactories()
        else -> emptyMap()
    }


    public companion object : PluginFactory<PersistencePlugin> {
        override val tag: PluginTag = PluginTag("device.persistence", group = PluginTag.DATAFORGE_GROUP)
        override fun build(context: Context, meta: Meta): PersistencePlugin = PersistencePlugin(meta)
    }
}

/**
 * Convenience extension to get the [PersistencePlugin] from a context.
 * Throws an error if the plugin is not installed, as persistence is often a critical service.
 */
public val Context.persistence: PersistencePlugin
    get() = plugins.find(true) { it is PersistencePlugin } as? PersistencePlugin
        ?: error("PersistencePlugin plugin is not installed in the context.")