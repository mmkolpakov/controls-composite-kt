package space.kscience.controls.composite.persistence

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A simple, in-memory implementation of [SnapshotStore].
 * State is lost when the application terminates. Useful for testing and temporary storage.
 */
public class InMemorySnapshotStore : SnapshotStore {
    private val storage = mutableMapOf<Name, Meta>()
    private val mutex = Mutex()

    override suspend fun save(name: Name, snapshot: Meta) {
        mutex.withLock {
            storage[name] = snapshot
        }
    }

    override suspend fun load(name: Name): Meta? = mutex.withLock {
        storage[name]
    }

    override suspend fun delete(name: Name) {
        mutex.withLock {
            storage.remove(name)
        }
    }
}