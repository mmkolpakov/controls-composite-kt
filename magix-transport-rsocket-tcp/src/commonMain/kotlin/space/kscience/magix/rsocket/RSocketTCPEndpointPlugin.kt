package space.kscience.magix.rsocket

import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.magix.api.MagixEndpointFactory

/**
 * A DataForge plugin that registers a factory for creating RSocket-based Magix endpoints over TCP.
 */
public class RSocketTCPEndpointPlugin : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    override fun content(target: String): Map<Name, Any> = when (target) {
        MagixEndpointFactory.TARGET -> mapOf(
            "rsocket.tcp".asName() to RSocketWithTcpEndpointFactory
        )
        else -> emptyMap()
    }

    public companion object : PluginFactory<RSocketTCPEndpointPlugin> {
        override val tag: PluginTag = PluginTag("magix.endpoint.rsocket.tcp", group = PluginTag.DATAFORGE_GROUP)
        override fun build(context: Context, meta: Meta): RSocketTCPEndpointPlugin = RSocketTCPEndpointPlugin()
    }
}