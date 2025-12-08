package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A new feature indicating that the device can execute `dataforge-data` Tasks.
 * This enables complex, multi-step data processing and analysis workflows to be
 * triggered as device actions.
 *
 * @property supportedTaskBlueprints A list of `dataforge-data` Task Blueprint IDs that this device can execute.
 */
@Serializable
@SerialName("feature.taskExecutor")
public data class TaskExecutorFeature(
    val supportedTaskBlueprints: List<String>
) : Feature {
    override val capability: String get() = "space.kscience.controls.composite.model.contracts.device.TaskExecutorDevice"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
