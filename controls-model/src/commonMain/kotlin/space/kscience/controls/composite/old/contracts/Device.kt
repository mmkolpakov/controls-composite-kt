package space.kscience.controls.composite.old.contracts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.state.IState
import space.kscience.controls.composite.old.ExecutionContext
import space.kscience.controls.composite.old.InternalControlsApi
import space.kscience.controls.composite.old.SystemPrincipal
import space.kscience.controls.composite.old.contracts.Device.Companion.CHILD_DEVICE_TARGET
import space.kscience.controls.composite.old.lifecycle.DeviceLifecycleState
import space.kscience.controls.composite.old.lifecycle.ManagedComponent
import space.kscience.controls.composite.old.messages.DeviceMessage
import space.kscience.controls.composite.old.meta.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ObservableMeta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.provider.Provider
import kotlin.time.Clock

/**
 * A contract for a device that exposes properties.
 */
public interface PropertyDevice {
    /**
     * A collection of descriptors for all properties supported by this device.
     */
    public val propertyDescriptors: Collection<PropertyDescriptor>

    /**
     * Reads the physical value of a property. This operation may involve I/O and is therefore suspendable.
     * Upon successful read, it should also update the logical state and emit a [PropertyChangedMessage].
     *
     * @param propertyName The name of the property to read.
     * @param context The execution context, providing security and tracing information.
     * @return The value of the property as a [Meta] object.
     * @throws space.kscience.controls.composite.old.CompositeHubException if the property does not exist or a read error occurs.
     */
    @InternalControlsApi
    public suspend fun readProperty(propertyName: Name, context: ExecutionContext = ExecutionContext(SystemPrincipal)): Meta

    /**
     * Writes a new value to a mutable property. This is a suspendable operation.
     *
     * @param propertyName The name of the property to write.
     * @param value The new value to set.
     * @param context The execution context, providing security and tracing information.
     * @throws space.kscience.controls.composite.old.CompositeHubException if the property is not mutable or a write error occurs.
     */
    @InternalControlsApi
    public suspend fun writeProperty(propertyName: Name, value: Meta, context: ExecutionContext = ExecutionContext(SystemPrincipal))
}

/**
 * A contract for a device that exposes actions.
 */
public interface ActionDevice {
    /**
     * A collection of descriptors for all actions supported by this device.
     */
    public val actionDescriptors: Collection<ActionDescriptor>

    /**
     * Executes a device-specific action.
     *
     * @param actionName The name of the action to execute.
     * @param argument An optional [Meta] object containing arguments for the action.
     * @param context The execution context, providing security and tracing information.
     * @return An optional [Meta] object representing the result of the action. Returns `null` if the action does not produce a result.
     * @throws space.kscience.controls.composite.old.CompositeHubException if the action is not supported or fails during execution.
     */
    @InternalControlsApi
    public suspend fun execute(
        actionName: Name,
        argument: Meta? = null,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Meta?
}

/**
 * A general interface describing a managed Device. A [Device] instance serves as a [CoroutineScope]
 * for all its internal operations. It also acts as a [Provider] for its children, properties, and actions,
 * enabling seamless integration with the DataForge ecosystem.
 *
 * The device's lifecycle is formally defined and managed by a Finite State Machine (FSM).
 * Commands to change the lifecycle state (e.g., start, stop) are sent as events to this FSM,
 * typically by a [CompositeDeviceHub]. The current state of the lifecycle is exposed reactively
 * via the [lifecycleState] property from the [ManagedComponent] interface.
 *
 * ### Communication Model: Hybrid RPC and Event Stream
 *
 * This interface employs a hybrid communication old that combines the simplicity of RPC-style method calls
 * with the observability of an event stream, aligning with the Command Query Responsibility Segregation (CQRS) pattern.
 *
 * - **Commands and Queries (RPC-style):** The methods `readProperty`, `writeProperty`, and `execute` define a clear,
 *   synchronous-looking (but asynchronous) contract for interactions. This provides a straightforward API for developers.
 *   **Important:** While these look like direct method calls, a compliant runtime
 *   **should** translate these calls into internal, serializable request messages. This ensures that in a
 *   distributed system, all interactions can be transmitted over a network and audited.
 *
 * - **Notifications (Event Stream):** The `messageFlow` property provides a one-way, hot [SharedFlow] of [DeviceMessage]s.
 *   This flow broadcasts asynchronous notifications about state changes, such as property updates, lifecycle events,
 *   and errors. It is not used for requests or direct command responses.
 *
 */
public interface Device : ManagedComponent, CoroutineScope, PropertyDevice, ActionDevice, Provider {
    /**
     * Companion object holding stable identifiers for the capability.
     */
    public companion object {
        /**
         * The unique, fully-qualified name for the [Device] capability.
         * Used for feature detection and serialization.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.old.contracts.Device"

        /** DataForge provider target for accessing child devices. */
        public const val CHILD_DEVICE_TARGET: String = "child"
        /** DataForge provider target for accessing property descriptors. */
        public const val PROPERTY_TARGET: String = PropertyDescriptor.TYPE
        /** DataForge provider target for accessing action descriptors. */
        public const val ACTION_TARGET: String = ActionDescriptor.TYPE
    }

    /**
     * The local name of this device instance within its parent hub.
     * This name is used for addressing within the hub's scope.
     */
    public val name: Name

    /**
     * The configuration meta for the device. This is an [ObservableMeta], meaning that the device
     * can react to configuration changes in real-time without requiring a restart. The runtime
     * is responsible for constructing this meta by layering blueprint, child, and attachment configurations.
     */
    public val meta: ObservableMeta

    /**
     * A reactive [StateFlow] representing the current state of the device's lifecycle.
     * This provides a safe, observable way to track the device's status (e.g., Stopped, Running, Failed)
     * without exposing the underlying state machine implementation.
     */
    override val lifecycleState: StateFlow<DeviceLifecycleState>

    /**
     * A reactive [StateFlow] representing the current state of the device's operational FSM.
     * Returns `null` if the device does not have an operational FSM. This flow allows external
     * observers to react to the internal business logic state of the device (e.g., `IDLE`, `MOVING`, `ACQUIRING`).
     */
    public val operationalState: StateFlow<IState>?

    /**
     * A boolean flag indicating if the device is currently in the [DeviceLifecycleState.Running] state.
     * A convenience accessor for `lifecycleState.value == DeviceLifecycleState.Running`.
     */
    override val isRunning: Boolean get() = lifecycleState.value == DeviceLifecycleState.Running

    /**
     * A hot flow of messages originating from this device. This includes property changes,
     * action results, log messages, and lifecycle events. The flow is shared and may have a replay cache.
     */
    public val messageFlow: SharedFlow<DeviceMessage>

    /**
     * The clock associated with this device. It may be a system clock, a virtual clock for simulations,
     * or a compressed-time clock. This clock **must** be used as the source for all timestamps
     * in [StateValue] updates originating from this device to ensure time consistency.
     */
    public val clock: Clock

    /**
     * Provides content for DataForge's [Provider] mechanism. A runtime implementation of [Device]
     * **must** override this method to expose its properties, actions, and child devices for introspection.
     * The default implementation returns an empty map.
     *
     * Standard targets:
     * - [PROPERTY_TARGET]: Exposes [PropertyDescriptor]s.
     * - [ACTION_TARGET]: Exposes [ActionDescriptor]s.
     * - [CHILD_DEVICE_TARGET]: Exposes child [Device]s.
     *
     * @param target A string identifier for the type of content being requested.
     * @return A map of named content items.
     */
    override fun content(target: String): Map<Name, Any> = emptyMap()

    /**
     * Attempts to programmatically post a new [Event] to the device's operational FSM, if it exists.
     * This is the primary mechanism for actions and internal logic to interact with the operational state.
     * The default implementation is provided by the runtime and checks for the existence of an operational FSM.
     *
     * @param event The operational event to post.
     * @return `true` if the device has an operational FSM and the event was posted, `false` otherwise.
     */
    public suspend fun tryPostOperationalEvent(event: Event): Boolean {
        // The default implementation should be provided by the runtime, which has access to the FSM.
        // This default serves as a fallback for simple or mock device implementations where no FSM exists.
        return false
    }
}

// Type-safe extensions for interacting with devices using specifications.

/**
 * Reads the value of a property specified by [spec]. This is the primary, type-safe way to read a property.
 *
 * @param spec The [DevicePropertySpec] defining the property to read.
 * @return The property value of type [T].
 * @throws space.kscience.controls.composite.old.DevicePropertyException if the read fails or the returned value is null.
 */
public suspend fun <D : Device, T> D.read(spec: DevicePropertySpec<D, T>): T {
    @OptIn(InternalControlsApi::class)
    val meta = readProperty(spec.name)
    return spec.converter.read(meta)
}

/**
 * Writes a value to a mutable property specified by [spec]. This is the primary, type-safe way to write a property.
 *
 * @param spec The [MutableDevicePropertySpec] defining the property to write.
 * @param value The value of type [T] to write.
 */
public suspend fun <D : Device, T> D.write(spec: MutableDevicePropertySpec<D, T>, value: T) {
    @OptIn(InternalControlsApi::class)
    writeProperty(spec.name, spec.converter.convert(value))
}

/**
 * Executes an action specified by [spec] with the given [input]. This is the primary, type-safe way to execute an action.
 *
 * @param spec The [DeviceActionSpec] defining the action to execute.
 * @param input The input argument for the action.
 * @return The result of the action, or `null` if the action does not return a value.
 */
public suspend fun <D : Device, I, O> D.execute(spec: DeviceActionSpec<D, I, O>, input: I): O? {
    @OptIn(InternalControlsApi::class)
    val resultMeta = execute(spec.name, spec.inputConverter.convert(input))
    return resultMeta?.let { spec.outputConverter.read(it) }
}

/**
 * Executes an action that takes no input ([Unit]).
 * @see execute
 */
public suspend fun <D : Device, O> D.execute(spec: DeviceActionSpec<D, Unit, O>): O? = execute(spec, Unit)