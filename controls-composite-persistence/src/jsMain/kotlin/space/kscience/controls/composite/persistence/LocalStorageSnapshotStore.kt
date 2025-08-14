package space.kscience.controls.composite.persistence

import kotlinx.browser.localStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.w3c.dom.set
import space.kscience.dataforge.io.JsonMetaFormat
import space.kscience.dataforge.io.parse
import space.kscience.dataforge.io.toString
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.toStringUnescaped

/**
 * A [SnapshotStore] implementation that saves snapshots to the browser's localStorage.
 * Data persists across browser sessions.
 * Note: localStorage has size limits (usually around 5-10 MB).
 *
 * @param keyPrefix A prefix to be used for all keys in localStorage to avoid collisions.
 */
internal class LocalStorageSnapshotStore(private val keyPrefix: String = "controls.snapshot.") : SnapshotStore {
    private val mutex = Mutex()
    private val metaFormat = JsonMetaFormat

    private fun nameToKey(name: Name): String = "$keyPrefix${name.toStringUnescaped()}"

    override suspend fun save(name: Name, snapshot: Meta) {
        mutex.withLock {
            try {
                val key = nameToKey(name)
                val snapshotString = snapshot.toString(metaFormat)
                localStorage[key] = snapshotString
            } catch (e: Exception) {
                throw RuntimeException("Failed to save snapshot for '$name' to localStorage. Reason: ${e.message}", e)
            }
        }
    }

    override suspend fun load(name: Name): Meta? = mutex.withLock {
        try {
            val key = nameToKey(name)
            localStorage.getItem(key)?.let { snapshotString ->
                metaFormat.parse(snapshotString)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to load snapshot for '$name' from localStorage. Reason: ${e.message}", e)
        }
    }

    override suspend fun delete(name: Name) {
        mutex.withLock {
            try {
                val key = nameToKey(name)
                localStorage.removeItem(key)
            } catch (e: Exception) {
                throw RuntimeException("Failed to delete snapshot for '$name' from localStorage. Reason: ${e.message}", e)
            }
        }
    }
}