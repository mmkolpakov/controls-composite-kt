package space.kscience.controls.composite.persistence

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.debug
import space.kscience.dataforge.context.error
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.io.JsonMetaFormat
import space.kscience.dataforge.io.parse
import space.kscience.dataforge.io.toString
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.toStringUnescaped

/**
 * A Native implementation of [SnapshotStore] that saves snapshots to files on the local filesystem
 * using `okio` for file operations and `dataforge-io` for serialization.
 *
 * @param context The context, used for logging.
 * @param basePath The base directory where snapshots will be stored.
 */
public class FileSnapshotStore(
    private val context: Context,
    basePath: String,
) : SnapshotStore {
    private val fs: FileSystem = FileSystem.SYSTEM
    private val basePath: Path = basePath.toPath()
    private val mutex = Mutex()
    private val metaFormat = JsonMetaFormat

    init {
        if (!fs.exists(this.basePath)) {
            context.logger.info { "Creating snapshot directory at '${this.basePath}'" }
            fs.createDirectories(this.basePath, mustCreate = false)
        }
    }

    private fun nameToPath(name: Name): Path = this.basePath / "${name.toStringUnescaped()}.json"

    override suspend fun save(name: Name, snapshot: Meta) {
        mutex.withLock {
            try {
                val path = nameToPath(name)
                val snapshotString = snapshot.toString(metaFormat)
                fs.write(path) {
                    writeUtf8(snapshotString)
                }
                context.logger.debug { "Saved snapshot for '$name' to '$path'." }
            } catch (e: Exception) {
                context.logger.error(e) { "Failed to save snapshot for '$name'." }
            }
        }
    }

    override suspend fun load(name: Name): Meta? = mutex.withLock {
        val path = nameToPath(name)
        if (fs.exists(path)) {
            try {
                val snapshotString = fs.read(path) {
                    readUtf8()
                }
                metaFormat.parse(snapshotString).also {
                    context.logger.debug { "Loaded snapshot for '$name' from '$path'." }
                }
            } catch (e: Exception) {
                context.logger.error(e) { "Failed to load snapshot for '$name' from '$path'." }
                null
            }
        } else {
            null
        }
    }

    override suspend fun delete(name: Name) {
        mutex.withLock {
            val path = nameToPath(name)
            try {
                fs.delete(path, mustExist = false)
                context.logger.debug { "Deleted snapshot for '$name' at '$path'." }
            } catch (e: Exception) {
                context.logger.error(e) { "Failed to delete snapshot for '$name' at '$path'." }
            }
        }
    }
}