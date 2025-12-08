package space.kscience.controls.composite.model.alarms

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * Defines the severity level of an alarm, indicating its operational importance.
 */
@Serializable
public enum class AlarmSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * The fundamental state of the alarm condition, typically derived from physical sensors or logic.
 * This reflects the "Process State" according to ISA 18.2 standards.
 */
@Serializable
public enum class AlarmState {
    /**
     * The monitored condition is within normal parameters.
     */
    NORMAL,

    /**
     * The monitored condition is abnormal. The alarm is effectively "ON" from a physical perspective.
     */
    ACTIVE
}

/**
 * Status flags indicating how the alarm is being managed by the system or operator.
 * These flags exist orthogonally to the [AlarmState].
 *
 * @property isAcked True if the operator has acknowledged the current alarm activation.
 * @property isShelved True if the alarm is temporarily suppressed by the operator (e.g. "Snooze").
 *                     Shelved alarms do not appear in the main active list but are still tracked.
 * @property isSuppressed True if the alarm is suppressed by system logic (e.g., "Equipment Offline", "Maintenance Mode").
 *                        Suppressed alarms are not presented to the operator.
 * @property isLatched True if the alarm condition returned to [AlarmState.NORMAL], but the alarm logic
 *                     requires a manual reset. A latched alarm remains treated as active until explicitly reset.
 */
@Serializable
public data class AlarmStatus(
    val isAcked: Boolean = false,
    val isShelved: Boolean = false,
    val isSuppressed: Boolean = false,
    val isLatched: Boolean = false
)

/**
 * A complete snapshot of an alarm instance at a specific moment in time.
 * This object aggregates the static definition, the physical state, and the management status.
 *
 * @param name The unique name of the alarm within its scope.
 * @param severity The effective severity of the alarm. This may change dynamically based on logic.
 * @param state The physical state of the monitored condition (Normal/Active).
 * @param status The management status flags (Ack, Shelve, etc.).
 * @param lastTransitionTime Timestamp of the last change to either [state] or [status].
 * @param message Optional dynamic message describing the current specific condition (e.g. "Value 105 > Limit 100").
 */
@Serializable
public data class ActiveAlarm(
    val name: Name,
    val severity: AlarmSeverity,
    val state: AlarmState,
    val status: AlarmStatus,
    val lastTransitionTime: Instant,
    val message: String? = null
)
