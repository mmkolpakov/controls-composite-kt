package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.nsk.kstatemachine.statemachine.StateMachineDslMarker
import space.kscience.controls.composite.model.Permission
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import kotlin.reflect.KType

/**
 * A serializable, self-contained descriptor for a device property. This object provides all the static information
 * about a property, making it suitable for introspection, UI generation, and validation without needing a live
 * device instance.
 *
 * @property name The unique, potentially hierarchical name of the property. Uses [Name] for consistency with DataForge.
 * @property valueTypeName The string representation of the property's [KType]. Essential for runtime type validation
 *                         in dynamic environments without reflection.
 * @property description An optional human-readable description of the property.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property metaDescriptor A descriptor for the [Meta] value of the property, defining its structure and constraints.
 * @property readable Indicates if the property can be read. Defaults to `true`.
 * @property mutable Indicates if the property can be written to. Defaults to `false`.
 * @property permissions The set of permissions required to access (read or write) this property.
 * @property metrics A [Meta] block for configuring metrics collection for this property (e.g., enabling counters, gauges).
 * @property labels A map of static labels to be attached to any metrics generated for this property.
 * @property persistent Indicates if this property's state is required to be included in device snapshots and restored
 *                      when the device starts if persistence is enabled.
 * @property transient Indicates if this property's state must NOT be included in device snapshots. Overrides [persistent] if both are set.
 */
@Serializable
public data class PropertyDescriptor(
    public val name: Name,
    public val valueTypeName: String,
    public val description: String? = null,
    public val help: String? = null,
    public val metaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val readable: Boolean = true,
    public val mutable: Boolean = false,
    public val permissions: Set<Permission> = emptySet(),
    public val metrics: Meta = Meta.EMPTY,
    public val labels: Map<String, String> = emptyMap(),
    public val persistent: Boolean = false,
    public val transient: Boolean = false,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A serializable, self-contained descriptor for a device action. This object provides all the static information
 * about an action, which can be used for UI generation, validation, and remote invocation.
 *
 * @property name The unique, potentially hierarchical name of the action. Uses [Name] for consistency.
 * @property description An optional human-readable description of the action.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property meta Additional arbitrary metadata for the action. For example, to store a serialized transaction plan.
 * @property inputMetaDescriptor A descriptor for the action's input [Meta].
 * @property outputMetaDescriptor A descriptor for the action's output [Meta].
 * @property taskBlueprintId If non-null, indicates that this action is implemented by a `dataforge-data` [Task]
 *                           with the specified blueprint ID. This enables deeper integration with DataForge's
 *                           computation and analysis ecosystem.
 * @property permissions The set of permissions required to execute this action.
 * @property metrics A [Meta] block for configuring metrics collection for this action (e.g., invocation counts, execution time).
 * @property labels A map of static labels to be attached to any metrics generated for this action.
 * @property operationalEventTypeName The FQN of an Event to post to the operational FSM on action execution.
 * @property operationalEventMeta Optional [Meta] to construct the operational event.
 * @property operationalSuccessEventTypeName The FQN of an Event to post on successful action execution.
 * @property operationalSuccessEventMeta Optional [Meta] to construct the success event.
 * @property operationalFailureEventTypeName The FQN of an Event to post on failed action execution.
 * @property operationalFailureEventMeta Optional [Meta] to construct the failure event.
 * @property taskInputTypeName String representation of the input KType for a Task-based action.
 * @property taskOutputTypeName String representation of the output KType for a Task-based action.
 */
@Serializable
public data class ActionDescriptor(
    public val name: Name,
    public val description: String? = null,
    public val help: String? = null,
    public val meta: Meta = Meta.EMPTY,
    public val inputMetaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val outputMetaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val taskBlueprintId: String? = null,
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
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
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