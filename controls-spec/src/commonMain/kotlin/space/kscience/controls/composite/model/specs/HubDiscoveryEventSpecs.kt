package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.*

/**
 * An event representing a change in the set of available hubs in the network.
 * Implemented as a sealed interface for polymorphism and extensibility.
 */
@Serializable
public sealed interface HubDiscoveryEvent : MetaRepr {
    public val hubId: String
}

/**
 * Fired when a new hub is discovered or an existing hub comes online.
 */
@Serializable
@SerialName("hub.up")
public data class HubUp(
    override val hubId: String,
    /**
     * A network-specific address for the hub (e.g., "host:port", "topic", etc.).
     */
    val address: String,
    /**
     * Additional metadata about the hub, such as supported features or versions.
     */
    val hubMeta: Meta? = null,
) : HubDiscoveryEvent {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Fired when a previously discovered hub goes offline or is no longer reachable.
 */
@Serializable
@SerialName("hub.down")
public data class HubDown(
    override val hubId: String,
) : HubDiscoveryEvent {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
