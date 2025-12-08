package space.kscience.controls.composite.model.state

import kotlinx.serialization.Serializable

/**
 * General status of data validity, inspired by OPC-UA and industrial standards.
 */
@Serializable
public enum class QualityStatus {
    /**
     * The value is valid, reliable, and current.
     */
    GOOD,

    /**
     * The value is not useful or usable. This may indicate sensor failure,
     * configuration error, or communication breakdown.
     */
    BAD,

    /**
     * The quality of the value is unknown or has low confidence.
     * This might happen during initialization, value substitution, or out-of-range conditions
     * where the value is technically present but may not be accurate.
     */
    UNCERTAIN
}

/**
 * A detailed quality indicator for a piece of state data.
 *
 * @property status The high-level validity status.
 * @property code A machine-readable string code indicating the specific reason (e.g., "COMM_FAILURE", "OVERFLOW", "INITIAL").
 *                If null, the generic status is sufficient.
 * @property description Optional human-readable description of the quality state.
 */
@Serializable
public data class DataQuality(
    val status: QualityStatus,
    val code: String? = null,
    val description: String? = null
) {
    public companion object {
        /** Predefined constant for Good quality. */
        public val GOOD: DataQuality = DataQuality(QualityStatus.GOOD)

        /** Predefined constant for Bad quality due to communication failure. */
        public val BAD_COMMUNICATION: DataQuality = DataQuality(QualityStatus.BAD, "COMM_FAILURE")

        /** Predefined constant for Bad quality due to sensor failure. */
        public val BAD_SENSOR: DataQuality = DataQuality(QualityStatus.BAD, "SENSOR_FAILURE")

        /** Predefined constant for Uncertain quality during initialization (no value received yet). */
        public val UNCERTAIN_INITIAL: DataQuality = DataQuality(QualityStatus.UNCERTAIN, "INITIAL")

        /** Predefined constant for Uncertain quality when the value is substituted or simulated. */
        public val UNCERTAIN_SUBSTITUTE: DataQuality = DataQuality(QualityStatus.UNCERTAIN, "SUBSTITUTE")
    }
}
