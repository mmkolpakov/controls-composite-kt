package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get

/**
 * A factory function to create a platform-specific [SnapshotStore] based on configuration.
 */
internal expect fun createSnapshotStore(context: Context, meta: Meta): SnapshotStore

/**
 * A DataForge plugin that provides a [SnapshotStore] for device state persistence.
 * The type of store is configured via the plugin's metadata.
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

    /**
     * The configured snapshot store instance.
     * Defaults to [InMemorySnapshotStore] if no configuration is provided.
     */
    public val store: SnapshotStore by lazy {
        val storeMeta = meta["store"] ?: Meta.EMPTY
        createSnapshotStore(context, storeMeta)
    }

    public companion object : PluginFactory<PersistencePlugin> {
        override val tag: PluginTag = PluginTag("device.persistence", group = PluginTag.DATAFORGE_GROUP)
        override fun build(context: Context, meta: Meta): PersistencePlugin = PersistencePlugin(meta)
    }
}

/**
 * Convenience extension to get the [PersistencePlugin] from a context.
 * Returns null if the plugin is not installed.
 */
public val Context.persistence: PersistencePlugin?
    get() = plugins.find(true) { it is PersistencePlugin } as? PersistencePlugin