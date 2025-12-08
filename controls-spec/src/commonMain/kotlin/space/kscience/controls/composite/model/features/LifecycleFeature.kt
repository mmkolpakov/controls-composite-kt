package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.FsmDescriptor
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A feature describing the lifecycle management capabilities of a device.
 *
 * @property descriptor The introspected structure of the lifecycle FSM.
 */
@Serializable
@SerialName("feature.lifecycle")
public data class LifecycleFeature(
    val descriptor: FsmDescriptor,
) : Feature {
    override val capability: String get() = "space.kscience.controls.composite.model.contracts.device.Device"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
