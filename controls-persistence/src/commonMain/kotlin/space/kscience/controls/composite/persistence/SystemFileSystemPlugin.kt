package space.kscience.controls.composite.persistence

import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A DataForge plugin that registers the default platform-specific [SystemFileSystemFactory].
 * This allows the [FileSystemManager] to discover and provide `FileSystem.SYSTEM` by default.
 */
public class SystemFileSystemPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    override fun content(target: String): Map<Name, Any> = when (target) {
        FileSystemManager.FILESYSTEM_FACTORY_TARGET -> mapOf(
            SystemFileSystemFactory.name to SystemFileSystemFactory
        )
        else -> emptyMap()
    }

    public companion object : PluginFactory<SystemFileSystemPlugin> {
        override val tag: PluginTag = PluginTag("filesystem.system", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): SystemFileSystemPlugin = SystemFileSystemPlugin()
    }
}