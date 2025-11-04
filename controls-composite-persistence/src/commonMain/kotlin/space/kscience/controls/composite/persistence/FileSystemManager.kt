package space.kscience.controls.composite.persistence

import okio.FileSystem
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A DataForge plugin that manages and provides named instances of Okio [FileSystem].
 * This plugin discovers [FileSystemFactory] implementations from the context and uses them
 * to create and cache [FileSystem] instances.
 */
public class FileSystemManager : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val factories by lazy {
        context.gather<FileSystemFactory>(FILESYSTEM_FACTORY_TARGET)
    }
    private val cache = mutableMapOf<Name, FileSystem>()

    /**
     * Retrieves or creates a [FileSystem] instance by its registered name.
     *
     * @param name The name of the file system factory to use.
     * @return The [FileSystem] instance.
     * @throws IllegalStateException if a factory for the given name is not found.
     */
    public fun get(name: Name): FileSystem = cache.getOrPut(name) {
        factories[name]?.build(context, meta)
            ?: error("FileSystem factory with name '$name' not found.")
    }

    /**
     * Manually registers a pre-built [FileSystem] instance with a given name.
     * This is primarily useful for testing to inject a [okio.fakefilesystem.FakeFileSystem].
     *
     * @param name The name to register the file system under.
     * @param fileSystem The [FileSystem] instance to register.
     */
    public fun register(name: Name, fileSystem: FileSystem) {
        cache[name] = fileSystem
    }

    public companion object : PluginFactory<FileSystemManager> {
        override val tag: PluginTag = PluginTag("filesystem.manager", group = PluginTag.DATAFORGE_GROUP)
        public const val FILESYSTEM_FACTORY_TARGET: String = "filesystem.factory"

        override fun build(context: Context, meta: Meta): FileSystemManager = FileSystemManager()
    }
}

/**
 * A convenience extension to get the [FileSystemManager] from a context.
 */
public val Context.fileSystemManager: FileSystemManager
    get() = request(FileSystemManager)