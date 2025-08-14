package space.kscience.controls.composite.model.lifecycle

import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.composite.model.SerializableDeviceFailure

/**
 * Defines the standard vocabulary of states for a device's lifecycle state machine.
 * This enum represents the *names* of the lifecycle states. The actual state machine
 * implementation (e.g., using KStateMachine) will create state objects corresponding
 * to these enum entries. This approach decouples the model from the FSM library.
 */
public enum class DeviceLifecycleState {
    /** The device blueprint is known, but no instance has been created or attached yet. */
    Detached,

    /** The device instance is being created, and its children are being recursively attached. */
    Attaching,

    /** The device is fully attached and initialized but not running. It is ready to be started. */
    Stopped,

    /** The device is executing its start sequence, including all pre-start logic. */
    Starting,

    /** The device is fully operational and running. */
    Running,

    /** The device is executing its stop sequence. */
    Stopping,

    /** The device has encountered an unrecoverable error and is not operational. It may be restarted or reset. */
    Failed,

    /** The device instance is being removed from the hub, and its resources are being released. */
    Detaching
}

/**
 * Defines the standard vocabulary of events that drive the device's lifecycle state machine.
 * These events correspond to commands issued by a [space.kscience.controls.composite.model.contracts.CompositeDeviceHub].
 */
public sealed interface DeviceLifecycleEvent : Event {
    /** A command to instantiate and attach the device to the hub. */
    public data object Attach : DeviceLifecycleEvent

    /** A command to start the device's operation. */
    public data object Start : DeviceLifecycleEvent

    /** A command to stop the device's operation. */
    public data object Stop : DeviceLifecycleEvent

    /** A command to reset the device from a [DeviceLifecycleState.Failed] state back to [DeviceLifecycleState.Stopped]. */
    public data object Reset : DeviceLifecycleEvent

    /** A command to detach and destroy the device instance. */
    public data object Detach : DeviceLifecycleEvent

    /**
     * An event indicating that an unrecoverable error has occurred.
     * @property failure A serializable representation of the error, suitable for network transmission and persistence.
     */
    public data class Fail(val failure: SerializableDeviceFailure) : DeviceLifecycleEvent
}