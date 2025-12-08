package space.kscience.controls.composite.model.services

import kotlinx.coroutines.flow.Flow
import space.kscience.controls.composite.model.specs.HubDiscoveryEvent
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that discovers other `CompositeDeviceHub` instances on the network.
 * This allows for dynamic, self-organizing clusters of control systems.
 * Implementations could use mDNS, ZooKeeper, Consul, or a static configuration file.
 */
public interface HubDiscoveryService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * A hot [Flow] that emits [HubDiscoveryEvent]s as hubs appear and disappear from the network.
     */
    public fun discover(): Flow<HubDiscoveryEvent>

    public companion object : PluginFactory<HubDiscoveryService> {
        override val tag: PluginTag = PluginTag("hub.discovery", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): HubDiscoveryService {
            error("HubDiscoveryService is a service interface and requires a runtime-specific implementation.")
        }
    }
}
