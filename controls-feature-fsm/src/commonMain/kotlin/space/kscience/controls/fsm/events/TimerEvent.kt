package space.kscience.controls.fsm.events

import kotlinx.serialization.Serializable
import ru.nsk.kstatemachine.event.Event
import kotlin.time.Duration

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