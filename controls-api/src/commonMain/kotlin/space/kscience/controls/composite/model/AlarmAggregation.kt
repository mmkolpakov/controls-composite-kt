package space.kscience.controls.composite.model.alarms

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.device.AlarmDescriptor

/**
 * A summary of active alarms within a scope, broken down by severity and acknowledgement status.
 * Critical for top-level dashboards.
 */
@Serializable
public data class AlarmCounts(
    val activeTotal: Int = 0,
    val activeUnacked: Int = 0,
    val activeAcked: Int = 0,
    val criticalUnacked: Int = 0,
    val highUnacked: Int = 0,
    val mediumUnacked: Int = 0,
    val lowUnacked: Int = 0,
    val shelved: Int = 0,
    val suppressed: Int = 0
)

/**
 * An interface for components (like DeviceHub or CompositeDevice) that aggregate alarm states
 * from their children. This enables hierarchical alarm management ("drill-down").
 */
public interface AlarmAggregator {
    /**
     * A reactive state representing the maximum severity among all active alarms in this aggregation scope.
     * If no alarms are active, the value is null.
     */
    public val maxSeverity: StateFlow<AlarmSeverity?>

    /**
     * A reactive summary of alarm counts.
     */
    public val alarmCounts: StateFlow<AlarmCounts>

    /**
     * A reactive list of descriptors for all currently active alarms within this scope.
     * This provides a flattened view of active issues for operators.
     * Note: For massive systems, this list might be truncated or paginated in the implementation.
     */
    public val activeAlarms: StateFlow<List<AlarmDescriptor>>
}
