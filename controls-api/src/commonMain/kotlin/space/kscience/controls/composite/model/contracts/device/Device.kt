package space.kscience.controls.composite.model.contracts.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import space.kscience.controls.composite.model.common.ExecutionContext
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.common.SystemPrincipal
import space.kscience.controls.composite.model.contracts.device.OperationalStateDescriptor
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState
import space.kscience.controls.composite.model.lifecycle.ManagedComponent
import space.kscience.controls.composite.model.messages.DeviceHubEvent
import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.controls.composite.model.specs.device.PropertyDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ObservableMeta
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.provider.Provider
import kotlin.time.Clock

/**
 * The fundamental contract for a managed component in the control system. A [Device] instance serves as a
 * [CoroutineScope] for all its internal operations. It represents a single, logical component, which may or may not
 * contain other devices. For devices that act as containers, see the [CompositeDevice] interface.
 *
 * This interface represents a **runtime contract** for a device, defining its observable state and behavior.
 * While it contains methods for interaction (`readProperty`, `execute`), these are marked as [InternalControlsApi]
 * and are not intended for general use. All public user-facing operations **must** be initiated through a
 * [space.kscience.controls.composite.model.contracts.hub.DeviceHub], which acts as the controller and entry point for all interactions. The internal methods
 * are primarily for testing, internal communication between linked devices, and use by the hub itself.
 *
 * @see ManagedComponent for lifecycle details.
 */
public interface Device : ManagedComponent, CoroutineScope, Provider {
    /** Companion object holding stable identifiers for the capability. */
    public companion object {
        /**
         * The unique, fully-qualified name for the [Device] capability.
         * Used for feature detection and serialization.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.model.contracts.device.Device"
    }

    /**
     * The local name of this device instance within its parent.
     * This name is used for addressing within the parent's scope.
     */
    public val name: Name

    /**
     * The configuration meta for the device. This is an [ObservableMeta], meaning that the device
     * can react to configuration changes in real-time. The runtime is responsible for constructing this
     * meta by layering blueprint, child, and attachment configurations.
     */
    public val meta: ObservableMeta

    /**
     * A collection of declarative, static descriptors for all properties supported by this device.
     * This is the single source of truth for the device's property contract.
     */
    public val propertyDescriptors: Collection<PropertyDescriptor>

    /**
     * A collection of declarative, static descriptors for all actions supported by this device.
     */
    public val actionDescriptors: Collection<ActionDescriptor>

    /**
     * A reactive [StateFlow] representing the current state of the device's lifecycle.
     */
    override val lifecycleState: StateFlow<DeviceLifecycleState>

    /**
     * A reactive [StateFlow] representing the connection and operational health of the device.
     * This allows supervisors to detect offline devices or devices requiring maintenance.
     */
    public val health: StateFlow<HealthState>

    /**
     * A reactive [StateFlow] representing the current state of the device's operational FSM, if one is defined.
     * It emits a serializable [OperationalStateDescriptor], making the state observable across network boundaries
     * without exposing the underlying FSM implementation details.
     * This allows external observers to react to the internal business logic state of the device
     * (e.g., `IDLE`, `MOVING`, `ACQUIRING`).
     */
    public val operationalState: StateFlow<OperationalStateDescriptor?>

    /**
     * A boolean flag indicating if the device is currently in the [DeviceLifecycleState.Running] state.
     */
    override val isRunning: Boolean get() = lifecycleState.value == DeviceLifecycleState.Running

    /**
     * A hot flow of events originating from this device and its children. Each event is wrapped in a [DeviceHubEvent],
     * which includes the `ExecutionContext` to preserve cross-cutting concerns like distributed tracing context.
     */
    public val messageFlow: SharedFlow<DeviceHubEvent>

    /**
     * The clock associated with this device. This clock **must** be used as the source for all timestamps
     * in [space.kscience.controls.composite.model.state.StateValue] updates originating from this device to ensure time consistency.
     */
    public val clock: Clock

    /**
     * Provides content for DataForge's [Provider] mechanism. A runtime implementation of [Device]
     * **must** override this method to expose its properties, actions, and child devices for introspection.
     * The default implementation returns an empty map.
     *
     * @param target A string identifier for the type of content being requested.
     * @return A map of named content items.
     */
    override fun content(target: String): Map<Name, Any> = emptyMap()

    /**
     * **Internal API:** Reads the physical value of a property. This operation may involve I/O and is therefore suspendable.
     * This method is not intended for public use; all external read requests must go through the [space.kscience.controls.composite.model.contracts.hub.DeviceHub].
     *
     * @param propertyName The name of the property to read.
     * @param context The execution context.
     * @return The value of the property as a [Meta] object.
     */
    @InternalControlsApi
    public suspend fun readProperty(propertyName: Name, context: ExecutionContext = ExecutionContext(SystemPrincipal)): Meta

    /**
     * **Internal API:** Writes a new value to a mutable property.
     * This method is not intended for public use; all external write requests must go through the [space.kscience.controls.composite.model.contracts.hub.DeviceHub].
     *
     * @param propertyName The name of the property to write.
     * @param value The new value to set.
     * @param context The execution context.
     */
    @InternalControlsApi
    public suspend fun writeProperty(propertyName: Name, value: Meta, context: ExecutionContext = ExecutionContext(SystemPrincipal))

    /**
     * **Internal API:** Executes a device-specific action.
     * This method is not intended for public use; all external action requests must go through the [space.kscience.controls.composite.model.contracts.hub.DeviceHub].
     *
     * @param actionName The name of the action to execute.
     * @param argument An optional [Meta] object containing arguments for the action.
     * @param context The execution context.
     * @return An optional [Meta] object representing the result of the action.
     */
    @InternalControlsApi
    public suspend fun execute(
        actionName: Name,
        argument: Meta? = null,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Meta?

    /**
     * Attempts to programmatically post a new signal to the device's operational logic.
     * This is an asynchronous, "fire-and-forget" operation for simple state transitions.
     *
     * @param signalName The logical name of the signal to send (e.g., "cancel", "pause").
     * @param argument Optional metadata to accompany the signal.
     * @return `true` if the device has a handler for the signal, `false` otherwise.
     */
    @InternalControlsApi
    public suspend fun postSignal(
        signalName: Name,
        argument: Meta? = null,
    ): Boolean
}
