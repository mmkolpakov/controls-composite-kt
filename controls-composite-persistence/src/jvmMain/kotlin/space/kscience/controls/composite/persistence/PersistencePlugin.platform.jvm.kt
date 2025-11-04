package space.kscience.controls.composite.persistence

import space.kscience.dataforge.names.Name

/**
 * The actual implementation for the JVM platform.
 * Returns an empty map because all common and JVM-specific factories are registered directly
 * in the `PersistencePlugin`'s `content` block.
 */
internal actual fun platformSnapshotStoreFactories(): Map<Name, SnapshotStoreFactory> = emptyMap()