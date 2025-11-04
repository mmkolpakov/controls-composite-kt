package space.kscience.controls.composite.model.lifecycle

import kotlinx.serialization.Serializable
import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.composite.model.SerializableDeviceFailure
import kotlin.time.Duration

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
    @Serializable
    public data object Attach : DeviceLifecycleEvent

    /** A command to start the device's operation. */
    @Serializable
    public data object Start : DeviceLifecycleEvent

    /** A command to stop the device's operation. */
    @Serializable
    public data object Stop : DeviceLifecycleEvent

    /** A command to reset the device from a [DeviceLifecycleState.Failed] state back to [DeviceLifecycleState.Stopped]. */
    @Serializable
    public data object Reset : DeviceLifecycleEvent

    /** A command to detach and destroy the device instance. */
    @Serializable
    public data object Detach : DeviceLifecycleEvent

    /**
     * An event indicating that an unrecoverable error has occurred.
     * @property failure A serializable representation of the error, suitable for network transmission and persistence.
     */
    @Serializable
    public data class Fail(val failure: SerializableDeviceFailure) : DeviceLifecycleEvent
}

/**
 * A sealed interface for events related to timers managed by the device's lifecycle context.
 * This allows for structured and extensible timer event handling within the FSM.
 */
@Serializable
public sealed interface TimerEvent : Event {
    /**
     * The unique name of the timer that generated this event.
     */
    public val timerName: String
}

/**
 * An event posted by the runtime at a regular interval for a named timer.
 * This event drives the logic in `onTimer` blocks within the FSM.
 *
 * @property timerName The unique name of the timer.
 * @property dt The actual [Duration] that has passed since the last tick. This value may vary slightly
 *              from the configured interval due to scheduler load, making it essential for accurate physics simulations.
 */
@Serializable
public data class TimerTickEvent(override val timerName: String, val dt: Duration) : TimerEvent