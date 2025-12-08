package space.kscience.controls.composite.model.specs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec

/**
 * Defines the source of addresses for a peer connection, allowing for both static and dynamic configurations.
 * By implementing [MetaRepr], all address sources can be consistently serialized to and from [space.kscience.dataforge.meta.Meta].
 */
@Serializable
public sealed interface AddressSource : MetaRepr

/**
 * A static, fixed list of addresses for a peer.
 *
 * @property addresses The list of potential addresses.
 */
@Serializable(with = StaticAddressSource.Serializer::class)
@SerialName("static")
public class StaticAddressSource : Scheme(), AddressSource {
    public var addresses: List<Address> by listOfConvertable(Address.serializer())

    public companion object : SchemeSpec<StaticAddressSource>(::StaticAddressSource)
    public object Serializer : SchemeAsMetaSerializer<StaticAddressSource>(Companion)
}

/**
 * A dynamic source of addresses provided by a `DiscoveryService`.
 *
 * @property serviceId The unique identifier of the service to look up. The runtime will use a `DiscoveryService`
 *                     plugin to resolve this ID to a list of available addresses.
 */
@Serializable(with = DiscoveredAddressSource.Serializer::class)
@SerialName("discovered")
public class DiscoveredAddressSource : Scheme(), AddressSource {
    public var serviceId: String by requiredString()

    public companion object : SchemeSpec<DiscoveredAddressSource>(::DiscoveredAddressSource)
    public object Serializer : SchemeAsMetaSerializer<DiscoveredAddressSource>(Companion)
}
