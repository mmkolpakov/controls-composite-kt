package space.kscience.controls.composite.persistence

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.controls.core.state.StatefulDevice
import space.kscience.dataforge.meta.Meta

/**
 * A feature describing the state persistence capabilities of a device.
 */
@Serializable
@SerialName("feature.stateful")
public data class StatefulFeature(
    val supportsHotRestore: Boolean = false,
) : Feature {
    override val capability: String get() = StatefulDevice.Companion.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}