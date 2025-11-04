package space.kscience.controls.composite.persistence

import space.kscience.dataforge.names.Name

/**
 * An expect declaration for a function that provides platform-specific [SnapshotStoreFactory] implementations.
 * This allows each platform (JVM, JS, Native) to register its own set of available snapshot stores.
 *
 * @return A map where the key is the factory's unique type name and the value is the factory instance.
 */
internal expect fun platformSnapshotStoreFactories(): Map<Name, SnapshotStoreFactory>