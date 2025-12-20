package space.kscience.controls.alarms

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