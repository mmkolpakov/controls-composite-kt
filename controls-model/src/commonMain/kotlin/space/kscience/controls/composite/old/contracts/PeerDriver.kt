package space.kscience.controls.composite.old.contracts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.addressing.Address
import space.kscience.controls.composite.old.ResiliencePolicy
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * Defines the source of addresses for a peer connection, allowing for both static and dynamic configurations.
 */
@Serializable
public sealed interface AddressSource

/**
 * A static, fixed list of addresses for a peer.
 * @property addresses The list of potential addresses.
 */
@Serializable
@SerialName("static")
public data class StaticAddressSource(val addresses: List<Address>) : AddressSource

/**
 * A dynamic source of addresses provided by a [DiscoveryService].
 * @property serviceId The unique identifier of the service to look up. The runtime will use a [DiscoveryService]
 *                     plugin to resolve this ID to a list of available addresses.
 */
@Serializable
@SerialName("discovered")
public data class DiscoveredAddressSource(val serviceId: String) : AddressSource

/**
 * Defines the strategy for selecting an address when multiple are available, particularly for failover.
 */
public enum class FailoverStrategy {
    /** Try addresses in the provided order until one succeeds. */
    ORDERED,

    /** Select a random address from the available list. */
    RANDOM,

    /** Use a round-robin scheduling approach. */
    ROUND_ROBIN
}

/**
 * A factory responsible for creating an instance of a [PeerConnection].
 *
 * @param P The type of the peer connection this driver creates.
 */
public fun interface PeerDriver<P : PeerConnection> {
    /**
     * Creates a new peer connection instance.
     * @param context The DataForge context for the connection.
     * @param meta The configuration meta for the connection.
     * @return A new instance of the peer connection.
     */
    public fun create(context: Context, meta: Meta): P
}

/**
 * A blueprint for a [PeerConnection]. This is a stateless factory that defines
 * how to create a peer connection instance, including support for service discovery, failover, and resilience.
 *
 * @param P The type of the peer connection this blueprint creates.
 */
public interface PeerBlueprint<P : PeerConnection> {
    /**
     * A unique identifier for this blueprint, typically derived from the property name in the DSL.
     */
    public val id: String

    /**
     * The source of network addresses for this peer. Can be static or dynamic.
     */
    public val addressSource: AddressSource

    /**
     * The strategy to use for selecting an address when multiple are available or for failover.
     */
    public val failoverStrategy: FailoverStrategy

    /**
     * An optional set of resilience policies to apply to this connection.
     * The runtime is responsible for implementing these policies (e.g., by wrapping the connection proxy).
     */
    public val resiliencePolicy: ResiliencePolicy? get() = null

    /**
     * The driver responsible for creating the [PeerConnection] instance.
     */
    public val driver: PeerDriver<P>
}

/**
 * A simple data-holding implementation of [PeerBlueprint].
 */
public data class SimplePeerBlueprint<P : PeerConnection>(
    override val id: String,
    override val addressSource: AddressSource,
    override val failoverStrategy: FailoverStrategy,
    override val resiliencePolicy: ResiliencePolicy? = null,
    override val driver: PeerDriver<P>,
) : PeerBlueprint<P>