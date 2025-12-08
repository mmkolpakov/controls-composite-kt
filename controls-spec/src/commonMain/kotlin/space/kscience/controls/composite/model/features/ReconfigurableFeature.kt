package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
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
    override val capability: String get() = "space.kscience.controls.composite.model.contracts.device.ReconfigurableDevice"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
