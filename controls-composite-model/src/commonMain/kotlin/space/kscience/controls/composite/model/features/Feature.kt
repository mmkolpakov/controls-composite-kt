package space.kscience.controls.composite.model.features

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.RestartPolicy
import space.kscience.controls.composite.model.contracts.*
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.state.StatefulDevice
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.descriptors.MetaDescriptor

/**
 * A base interface for a Feature descriptor. A feature provides structured, serializable metadata
 * about a specific capability of a device.
 *
 * This is an open, non-sealed interface annotated with `@Polymorphic` to allow users of the library
 * to define their own custom features in separate modules, ensuring the framework is extensible.
 */
@Polymorphic
public interface Feature : MetaRepr {
    /**
     * A fully qualified name of the capability interface this feature describes.
     * For example, `space.kscience.controls.composite.model.contracts.Device`.
     */
    public val capability: String
}

/**
 * A feature describing the lifecycle management capabilities of a device.
 */
@Serializable
@SerialName("feature.lifecycle")
public data class LifecycleFeature(
    val supportedStates: Set<String> = setOf(
        "Stopped",
        "Running",
        "Failed"
    ),
    val initialStateName: String = "Stopped",
) : Feature {
    override val capability: String get() = Device.CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

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
 * A feature indicating that the device has an operational Finite State Machine (FSM)
 * in addition to its lifecycle FSM. This FSM manages the device's internal logic and operational states.
 *
 * @property states A set of names for the states in the operational FSM.
 * @property events A set of event class names that can drive the operational FSM.
 */
@Serializable
@SerialName("feature.operationalFsm")
public data class OperationalFsmFeature(
    val states: Set<String>,
    val events: Set<String>,
) : Feature {
    override val capability: String = CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val CAPABILITY: String = "ru.nsk.kstatemachine.statemachine.StateMachine"
    }
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

/**
 * A feature indicating that the device can execute a [space.kscience.controls.composite.model.plans.TransactionPlan].
 * The runtime uses this feature to correctly dispatch plan-based actions to the device.
 */
@Serializable
@SerialName("feature.planExecutor")
public data class PlanExecutorFeature(
    override val capability: String = PlanExecutorDevice.CAPABILITY
) : Feature {
    override fun toMeta(): Meta = Meta {
        "capability" put capability
    }
}