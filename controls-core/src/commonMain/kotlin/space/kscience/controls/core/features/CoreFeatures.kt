package space.kscience.controls.core.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.contracts.ReconfigurableDevice
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.controls.core.state.StatefulDevice
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor

/**
 * A feature describing the device's ability to be reconfigured at runtime.
 */
@Serializable
@SerialName("feature.reconfigurable")
public data class ReconfigurableFeature(
    val reconfigDescriptor: MetaDescriptor = MetaDescriptor.EMPTY,
) : Feature {
    override val capability: String get() = ReconfigurableDevice.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
