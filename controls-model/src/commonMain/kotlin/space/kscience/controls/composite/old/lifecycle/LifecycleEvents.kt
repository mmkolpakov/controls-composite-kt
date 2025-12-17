package space.kscience.controls.composite.old.lifecycle

import kotlinx.serialization.Serializable
import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.core.faults.SerializableDeviceFailure
import kotlin.time.Duration

/**
 * Defines the standard vocabulary of events that drive the device's lifecycle state machine.
 * These events correspond to commands issued by a [space.kscience.controls.core.contracts.DeviceHub].
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

    /** A command to reset the device from a [space.kscience.controls.core.lifecycle.DeviceLifecycleState.Failed] state back to [space.kscience.controls.core.lifecycle.DeviceLifecycleState.Stopped]. */
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