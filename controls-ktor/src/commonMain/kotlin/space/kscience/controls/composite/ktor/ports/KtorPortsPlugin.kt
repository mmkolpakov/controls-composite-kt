package space.kscience.controls.composite.ktor.ports

import space.kscience.controls.composite.ports.PortManager
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

/**
 * A DataForge plugin for discovering and providing Ktor-based port implementations.
 * This plugin registers factories for TCP and UDP ports, allowing the [PortManager]
 * to create them from configuration.
 */
public class KtorPortsPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    override fun content(target: String): Map<Name, Any> = when (target) {
        PortManager.PORT_FACTORY_TARGET -> mapOf(
            "ktor.tcp".asName() to KtorTcpPort,
            "ktor.udp".asName() to KtorUdpPort
        )
        else -> emptyMap()
    }

    public companion object : PluginFactory<KtorPortsPlugin> {
        override val tag: PluginTag = PluginTag("ports.ktor", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): KtorPortsPlugin = KtorPortsPlugin()
    }
}