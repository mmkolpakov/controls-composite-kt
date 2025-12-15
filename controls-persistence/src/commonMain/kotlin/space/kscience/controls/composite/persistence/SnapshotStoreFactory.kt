package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta

/**
 * A factory for creating [SnapshotStore] instances from a [Meta] configuration.
 * This interface allows for a plug-in architecture for different persistence backends.
 * Implementations of this factory are discovered by the [PersistencePlugin] via the [SNAPSHOT_STORE_FACTORY_TARGET] constant.
 */
public interface SnapshotStoreFactory : Factory<SnapshotStore> {
    /**
     * The unique type string that identifies this factory in a configuration block.
     * For example, "file", "memory", or "localStorage".
     */
    public val type: String

    /**
     * Builds a [SnapshotStore] instance.
     *
     * @param context The context in which the store will operate.
     * @param meta The configuration specific to this store instance.
     * @return A new instance of [SnapshotStore].
     */
    override fun build(context: Context, meta: Meta): SnapshotStore

    public companion object {
        /**
         * The target string used by the plugin system to discover [SnapshotStoreFactory] providers.
         */
        public const val SNAPSHOT_STORE_FACTORY_TARGET: String = "snapshot.store.factory"
    }
}