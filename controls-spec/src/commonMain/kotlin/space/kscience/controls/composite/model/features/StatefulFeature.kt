package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.RestartPolicy
import space.kscience.dataforge.meta.Meta

/**
 * A feature describing the state persistence capabilities of a device.
 */
@Serializable
@SerialName("feature.stateful")
public data class StatefulFeature(
    val supportsHotRestore: Boolean = false,
    val defaultRestartPolicy: RestartPolicy = RestartPolicy(),
) : Feature {
    override val capability: String get() = "space.kscience.controls.composite.model.state.StatefulDevice"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
