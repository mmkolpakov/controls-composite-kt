package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.nsk.kstatemachine.statemachine.StateMachineDslMarker
import space.kscience.controls.composite.model.CachePolicy
import space.kscience.controls.composite.model.Permission
import space.kscience.controls.composite.model.ResourceLockSpec
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.QoS
import space.kscience.controls.composite.model.contracts.StreamPort
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.validation.ValidationRuleSpec
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name
import kotlin.reflect.KType
import kotlin.time.Duration

/**
 * A serializable, self-contained descriptor for a device property. This object provides all the static information
 * about a property, making it suitable for introspection, UI generation, and validation without needing a live
 * device instance.
 *
 * @property name The unique, potentially hierarchical name of the property. Uses [Name] for consistency with DataForge.
 * @property kind The semantic [PropertyKind], classifying the property's nature (e.g., physical, logical).
 * @property valueTypeName The string representation of the property's [KType]. Essential for runtime type validation
 *                         in dynamic environments without reflection.
 * @property description An optional human-readable description of the property.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property group An optional string for grouping related properties in a user interface.
 * @property icon An optional string identifier for a visual icon, used by UIs.
 * @property timeout The default timeout for both read and write operations on this property. The runtime is expected
 *                   to enforce this constraint. If null, a system-wide default may be used.
 * @property requiredLocks A list of resource locks that must be acquired by the runtime before this property can be
 *                         accessed. This is used to prevent concurrent access to shared resources.
 * @property metaDescriptor A descriptor for the [Meta] value of the property, defining its structure and constraints.
 * @property readable Indicates if the property can be read. Defaults to `true`.
 * @property mutable Indicates if the property can be written to. Defaults to `false`.
 * @property permissions The set of permissions required to access (read or write) this property.
 * @property metrics A [Meta] block for configuring metrics collection for this property (e.g., enabling counters, gauges).
 * @property labels A map of static labels to be attached to any metrics generated for this property.
 * @property validationRules A list of declarative, serializable validation rules to be enforced on this property.
 * @property persistent Indicates if this property's state is required to be included in device snapshots and restored
 *                      when the device starts if persistence is enabled.
 * @property transient Indicates if this property's state must NOT be included in device snapshots. Overrides [persistent] if both are set.
 * @property unit An optional string representing the physical unit of the property's value (e.g., "volts", "mm", "deg").
 * @property valueRange An optional range constraint for numeric properties, useful for UI widgets like sliders.
 * @property widgetHint A hint for UI generators suggesting a preferred widget type (e.g., "slider", "checkbox").
 * @property tags A set of extensible, semantic [MemberTag]s for classification by external systems (UI, documentation, etc.).
 * @property bindings A map of type-safe, protocol-specific configurations. The key is a unique string
 *                    identifying the protocol adapter (e.g., "modbus", "yandex"), and the value is the
 *                    serializable [AdapterBinding] configuration.
 */
@Serializable
public data class PropertyDescriptor(
    public val name: Name,
    public val kind: PropertyKind,
    public val valueTypeName: String,
    public val description: String? = null,
    public val help: String? = null,
    public val group: String? = null,
    public val icon: String? = null,
    public val timeout: @Contextual Duration? = null,
    public val requiredLocks: List<ResourceLockSpec> = emptyList(),
    public val metaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val readable: Boolean = true,
    public val mutable: Boolean = false,
    public val permissions: Set<Permission> = emptySet(),
    public val metrics: Meta = Meta.EMPTY,
    public val labels: Map<String, String> = emptyMap(),
    public val validationRules: List<ValidationRuleSpec> = emptyList(),
    public val persistent: Boolean = false,
    public val transient: Boolean = false,
    public val unit: String? = null,
    public val minValue: Double? = null,
    public val maxValue: Double? = null,
    public val widgetHint: String? = null,
    public val tags: Set<MemberTag> = emptySet(),
    public val bindings: Map<String, AdapterBinding> = emptyMap(),
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    /**
     * A non-serializable, convenient representation of the value range.
     * Reconstructed from minValue and maxValue.
     */
    @Transient
    val valueRange: ClosedRange<Double>? = if (minValue != null && maxValue != null) {
        minValue..maxValue
    } else null

    /**
     * A list of allowed values for this property, derived from the [metaDescriptor].
     */
    val allowedValues: List<Value>? by metaDescriptor::allowedValues

    /**
     * A list of allowed value types for this property, derived from the [metaDescriptor].
     */
    val valueTypes: List<ValueType>? by metaDescriptor::valueTypes

    public companion object {
        public const val TYPE: String = "property"
    }
}

/**
 * A serializable, self-contained descriptor for a device action. This object provides all the static information
 * about an action, which can be used for UI generation, validation, and remote invocation.
 *
 * @property name The unique, potentially hierarchical name of the action. Uses [Name] for consistency.
 * @property description An optional human-readable description of the action.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property group An optional string for grouping related actions in a user interface.
 * @property icon An optional string identifier for a visual icon, used by UIs.
 * @property defaultTimeout A recommended timeout for a single execution of this action's logic.
 * @property executionDeadline A hard deadline for the total duration of the action's execution.
 * @property requiredLocks A list of resource locks that must be acquired by the runtime before this action can be executed.
 * @property meta Additional arbitrary metadata for the action, like a serialized transaction plan.
 * @property inputMetaDescriptor A descriptor for the action's input [Meta].
 * @property outputDescriptor A descriptor for the action's output [Meta].
 * @property logicId If non-null, this action's implementation is provided by an external logic component.
 * @property logicVersionConstraint An optional version constraint for the external logic.
 * @property cachePolicy An optional policy defining how the results of this action should be cached.
 * @property taskBlueprintId If non-null, indicates that this action is implemented by a `dataforge-data` [Task].
 * @property distributable If `true`, a task-based action is a candidate for remote execution.
 * @property permissions The set of permissions required to execute this action.
 * @property metrics A [Meta] block for configuring metrics collection for this action.
 * @property labels A map of static labels to be attached to any metrics generated for this action.
 * @property operationalEventTypeName The FQN of an Event to post to the operational FSM on action execution.
 * @property operationalEventMeta Optional [Meta] to construct the operational event.
 * @property operationalSuccessEventTypeName The FQN of an Event to post on successful action execution.
 * @property operationalSuccessEventMeta Optional [Meta] to construct the success event.
 * @property operationalFailureEventTypeName The FQN of an Event to post on failed action execution.
 * @property operationalFailureEventMeta Optional [Meta] to construct the failure event.
 * @property taskInputTypeName String representation of the input KType for a Task-based action.
 * @property taskOutputTypeName String representation of the output KType for a Task-based action.
 * @property possibleFaults A set of FQNs of [DeviceFault]s that this action can predictably return.
 * @property requiredPredicates A set of names of predicate properties that must be `true` before this action can be executed.
 * @property tags A set of extensible, semantic [MemberTag]s for classification by external systems.
 * @property bindings A map of type-safe, protocol-specific configurations. The key is a unique string
 *                    identifying the protocol adapter, and the value is the serializable [AdapterBinding] configuration.
 */
@Serializable
public data class ActionDescriptor(
    public val name: Name,
    public val description: String? = null,
    public val help: String? = null,
    public val group: String? = null,
    public val icon: String? = null,
    public val defaultTimeout: @Contextual Duration? = null,
    public val executionDeadline: @Contextual Duration? = null,
    public val requiredLocks: List<ResourceLockSpec> = emptyList(),
    public val meta: Meta = Meta.EMPTY,
    public val inputMetaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val outputDescriptor: MetaDescriptor = MetaDescriptor(),
    public val logicId: Name? = null,
    public val logicVersionConstraint: String? = null,
    public val cachePolicy: CachePolicy? = null,
    public val taskBlueprintId: String? = null,
    public val distributable: Boolean = false,
    public val permissions: Set<Permission> = emptySet(),
    public val metrics: Meta = Meta.EMPTY,
    public val labels: Map<String, String> = emptyMap(),
    public val operationalEventTypeName: String? = null,
    public val operationalEventMeta: Meta? = null,
    public val operationalSuccessEventTypeName: String? = null,
    public val operationalSuccessEventMeta: Meta? = null,
    public val operationalFailureEventTypeName: String? = null,
    public val operationalFailureEventMeta: Meta? = null,
    public val taskInputTypeName: String? = null,
    public val taskOutputTypeName: String? = null,
    val possibleFaults: Set<String> = emptySet(),
    val requiredPredicates: Set<Name> = emptySet(),
    public val tags: Set<MemberTag> = emptySet(),
    public val bindings: Map<String, AdapterBinding> = emptyMap(),
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "action"
    }
}


/**
 * A serializable, self-contained descriptor for a device's data stream.
 * This object provides all the static information about a stream, making it suitable for
 * introspection and client discovery without needing a live device instance.
 *
 * @property name The unique, potentially hierarchical name of the stream.
 * @property description An optional human-readable description of the stream's purpose.
 * @property dataTypeFqName The fully-qualified class name of the primary data objects or frames
 *                          being transmitted over the stream. This serves as a hint for clients
 *                          on how to decode the raw byte stream.
 * @property permissions The set of permissions required to access (read from or write to) this stream.
 * @property suggestedRateHz An optional hint suggesting the expected data rate in Hertz. UI elements can use
 *                           this for scaling graphs or setting appropriate polling intervals.
 * @property direction An optional hint indicating the primary direction of data flow.
 * @property deliveryHint An optional hint suggesting the desired Quality of Service for the stream's transport.
 *                        The runtime may use this to select or configure the underlying transport mechanism.
 */
@Serializable
@DfType("device.stream")
public data class StreamDescriptor(
    public val name: Name,
    public val description: String? = null,
    public val dataTypeFqName: String,
    public val permissions: Set<Permission> = emptySet(),
    public val suggestedRateHz: Double? = null,
    public val direction: StreamDirection? = null,
    public val deliveryHint: QoS? = null,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "device.stream"
    }
}


/**
 * A specification for a device's read-only property. This is a behavioral contract
 * that defines how to interact with the property. It combines the static [PropertyDescriptor]
 * with the logic (`read` function) required to retrieve its value.
 *
 * @param D The type of the device this property belongs to. This is contravariant (`in`) to allow
 *          a spec defined for a supertype to be used with a subtype device.
 * @param T The type of the property's value.
 */
@StateMachineDslMarker
public interface DevicePropertySpec<in D : Device, T> {
    public val name: Name
    public val descriptor: PropertyDescriptor
    public val converter: MetaConverter<T>

    /**
     * The [KType] of the property's value.
     * Essential for compile-time or runtime type validation in bindings.
     * This property is populated by the DSL at creation time.
     */
    public val valueType: KType

    /** The logic to read the property's value from the device instance. */
    public suspend fun read(device: D): T?
}

/**
 * A specification for a mutable device property, extending the read-only version with write logic.
 */
@StateMachineDslMarker
public interface MutableDevicePropertySpec<in D : Device, T> : DevicePropertySpec<D, T> {
    /** The logic to write a new value to the property on the device instance. */
    public suspend fun write(device: D, value: T)
}

/**
 * A specification for a device's action.
 *
 * @param D The type of the device this action belongs to.
 * @param I The type of the action's input.
 * @param O The type of the action's output.
 */
@StateMachineDslMarker
public interface DeviceActionSpec<in D : Device, I, O> {
    public val name: Name
    public val descriptor: ActionDescriptor
    public val inputConverter: MetaConverter<I>
    public val outputConverter: MetaConverter<O>

    /**
     * The fully qualified name of an [ru.nsk.kstatemachine.event.Event] class to be posted to the device's operational FSM when this action is executed.
     * If this is non-null, the runtime can use this to drive the operational state machine
     * instead of, or in addition to, the `execute` logic. Storing the class name makes this
     * specification serializable.
     */
    public val operationalEventTypeName: String?

    /**
     * The fully qualified name of an [ru.nsk.kstatemachine.event.Event] class to be posted on successful action execution.
     */
    public val operationalSuccessEventTypeName: String?

    /**
     * The fully qualified name of an [ru.nsk.kstatemachine.event.Event] class to be posted on failed action execution.
     */
    public val operationalFailureEventTypeName: String?

    /** The logic to execute the action on the device instance. */
    public suspend fun execute(device: D, input: I): O?
}

/**
 * An enumeration defining the primary direction of data flow for a stream.
 * This serves as a hint for the runtime and UI generators.
 */
@Serializable
public enum class StreamDirection {
    /** The device primarily sends data through this stream. */
    OUT,

    /** The device primarily receives data through this stream. */
    IN,

    /** The stream is used for significant data transfer in both directions. */
    BIDIRECTIONAL
}


/**
 * A specification for a device's data stream. This is a behavioral contract that defines
 * how to access a stream. It combines the static [StreamDescriptor] with a factory function
 * (`get`) to create a live [StreamPort] instance.
 *
 * @param D The type of the device this stream belongs to.
 */
@StateMachineDslMarker
public interface DeviceStreamSpec<in D : Device> {
    public val name: Name
    public val descriptor: StreamDescriptor

    /**
     * A factory function that creates and returns a [StreamPort] for this data stream.
     *
     * **IMPORTANT CONTRACT:** The caller (the `runtime`) is fully responsible for managing the lifecycle of the
     * returned [StreamPort]. The `runtime` **must** call [StreamPort.close] when the owning device is
     * stopped, detached, or otherwise shut down to prevent resource leaks (e.g., open sockets or file handles).
     * The `get` function itself should only create the port, not manage it.
     */
    public val get: suspend D.() -> StreamPort
}