package space.kscience.controls.composite.persistence

import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

/**
 * The actual implementation for the JS platform.
 * It registers the [LocalStorageSnapshotStore] factory, making it discoverable by the [PersistencePlugin].
 */
internal actual fun platformSnapshotStoreFactories(): Map<Name, SnapshotStoreFactory> = mapOf(
    LocalStorageSnapshotStore.type.asName() to LocalStorageSnapshotStore
)