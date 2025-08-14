package space.kscience.controls.composite.ports

import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.asName

/**
 * A DataForge plugin responsible for creating and managing [Port] instances.
 * It acts as a factory, resolving port implementations based on configuration.
 */
public class PortManager : AbstractPlugin() {
    override val tag: PluginTag get() = Companion.tag

    private val factories by lazy {
        context.gather<Factory<Port>>(PORT_FACTORY_TARGET)
    }

    /**
     * Creates a new [Port] instance based on the provided metadata.
     * The 'type' property in the meta is used to find the appropriate factory.
     * @param meta The configuration for the port.
     * @return A new [Port] instance.
     * @throws IllegalStateException if a factory for the specified type is not found.
     */
    public fun newPort(meta: Meta): Port {
        val type = meta["type"].string ?: error("Port 'type' is not specified in the configuration.")
        val factory = factories[type.asName()]
            ?: error("A port factory for type '$type' is not registered in the context.")
        return factory.build(context, meta)
    }

    public companion object : PluginFactory<PortManager> {
        override val tag: PluginTag = PluginTag("device.ports", group = PluginTag.DATAFORGE_GROUP)
        public const val PORT_FACTORY_TARGET: String = "portFactory"

        override fun build(context: Context, meta: Meta): PortManager = PortManager()
    }
}

/**
 * A convenience extension to get the [PortManager] from a context.
 */
public val Context.ports: PortManager get() = request(PortManager)