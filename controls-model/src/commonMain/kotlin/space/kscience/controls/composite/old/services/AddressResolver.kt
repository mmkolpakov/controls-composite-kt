package space.kscience.controls.composite.old.services

import space.kscience.controls.composite.old.ExecutionContext
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A sealed interface representing the physical address details for a specific transport protocol.
 * This allows for type-safe handling of different connection parameters.
 */
public sealed interface TransportAddress

/**
 * Represents the physical address for a TCP-based connection.
 * @property host The hostname or IP address.
 * @property port The TCP port number.
 */
public data class TcpAddress(val host: String, val port: Int) : TransportAddress

/**
 * A contract for a service that resolves a logical hub identifier (`hubId`) into a physical [TransportAddress].
 * This is a critical component for service discovery in a distributed system, abstracting away the mechanism
 * of how addresses are found (e.g., from static configuration, DNS-SRV, a discovery service like Consul, etc.).
 */
public interface AddressResolver : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Resolves a logical `hubId` into a physical [TransportAddress].
     *
     * @param hubId The logical identifier of the hub to resolve.
     * @param context The [ExecutionContext] of the operation, providing additional context like security principal.
     * @return The resolved [TransportAddress], or `null` if the address cannot be found.
     */
    public suspend fun resolve(hubId: String, context: ExecutionContext): TransportAddress?

    public companion object : PluginFactory<AddressResolver> {
        override val tag: PluginTag = PluginTag("device.address.resolver", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime
         * or a dedicated service discovery module.
         */
        override fun build(context: Context, meta: Meta): AddressResolver {
            error("AddressResolver is a service interface and requires a runtime-specific implementation.")
        }
    }
}