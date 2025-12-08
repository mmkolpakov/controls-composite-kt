package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * An event representing a change in the set of available addresses for a discovered service.
 */
@Polymorphic
public interface AddressUpdateEvent : MetaRepr {
    public val serviceId: String
    public val address: Address
}

/**
 * Fired when a new address for a service is discovered.
 */
@Serializable
@SerialName("address.up")
public data class AddressUp(
    override val serviceId: String,
    override val address: Address,
) : AddressUpdateEvent {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Fired when a previously available address for a service is no longer reachable.
 */
@Serializable
@SerialName("address.down")
public data class AddressDown(
    override val serviceId: String,
    override val address: Address,
) : AddressUpdateEvent {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
