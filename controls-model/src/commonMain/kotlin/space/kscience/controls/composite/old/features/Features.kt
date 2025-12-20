package space.kscience.controls.composite.old.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.old.RestartPolicy
import space.kscience.controls.composite.old.contracts.PeerConnection
import space.kscience.controls.automation.PlanExecutorDevice
import space.kscience.controls.composite.old.contracts.ReconfigurableDevice
import space.kscience.controls.composite.old.contracts.TaskExecutorDevice
import space.kscience.controls.composite.old.state.StatefulDevice
import space.kscience.controls.core.contracts.Device
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.serialization.serializableToMeta
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

/**
 * A feature describing the state persistence capabilities of a device.
 */
@Serializable
@SerialName("feature.stateful")
public data class StatefulFeature(
    val supportsHotRestore: Boolean = false,
    val defaultRestartPolicy: RestartPolicy = RestartPolicy(),
) : Feature {
    override val capability: String get() = StatefulDevice.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A feature indicating that the device can expose its properties as a `dataforge-data` `DataSource`.
 * This enables seamless integration with the DataForge data processing and analysis ecosystem.
 * The runtime is responsible for providing the actual `DataSource` implementation based on this feature.
 *
 * @property dataTypeString The common upper bound type for all data items produced by this source, represented as a string.
 *                          The actual [KType] is not serializable and must be handled by the runtime.
 */
@Serializable
@SerialName("feature.dataSource")
public data class DataSourceFeature(
    val dataTypeString: String?,
) : Feature {
    override val capability: String get() = "space.kscience.dataforge.data.DataSource"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

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
    override val capability: String get() = TaskExecutorDevice.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A feature indicating that the device supports direct transfer of large binary data,
 * bypassing the standard message bus for efficiency. This is an analogue to `PeerConnection` from `controls-kt`.
 *
 * @property formats A list of supported binary content types or formats (e.g., "image/jpeg", "custom-binary-format").
 */
@Serializable
@SerialName("feature.binaryData")
public data class BinaryDataFeature(
    val formats: List<String> = emptyList()
) : Feature {
    override val capability: String = PeerConnection.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
