package space.kscience.controls.composite.persistence

import okio.FileSystem
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A factory for creating named instances of Okio [FileSystem].
 * This allows for a plug-in architecture for different FileSystem implementations.
 */
public interface FileSystemFactory : Factory<FileSystem> {
    /**
     * The unique name for this file system provider, used for configuration.
     */
    public val name: Name

    /**
     * Builds a [FileSystem] instance.
     */
    override fun build(context: Context, meta: Meta): FileSystem
}