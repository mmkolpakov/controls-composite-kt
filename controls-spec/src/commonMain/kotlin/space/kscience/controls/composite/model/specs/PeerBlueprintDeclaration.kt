package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.contracts.FailoverStrategy
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.ResiliencePolicy
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A pure, serializable, and platform-agnostic declaration of a peer connection blueprint.
 * This data class contains all static configuration for a peer, such as its address source,
 * failover strategy, and resilience policies, but intentionally excludes any executable logic
 * like a `driver`.
 *
 * The runtime is responsible for resolving the `driverId` to a concrete `PeerDriver` implementation.
 *
 * @property driverId A unique identifier for the `PeerDriver` implementation that the runtime should use
 *                    to create an instance of this peer connection.
 * @property addressSource The source of network addresses for this peer, which can be static or dynamic.
 * @property failoverStrategy The strategy for selecting an address when multiple are available or for failover.
 * @property resiliencePolicy An optional set of resilience policies (e.g., timeouts, retries) for the connection.
 * @property meta Additional, driver-specific configuration metadata.
 */
@Serializable
public data class PeerBlueprintDeclaration(
    val driverId: String,
    val addressSource: AddressSource,
    val failoverStrategy: FailoverStrategy = FailoverStrategy.ORDERED,
    val resiliencePolicy: ResiliencePolicy? = null,
    val meta: Meta = Meta.EMPTY
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
