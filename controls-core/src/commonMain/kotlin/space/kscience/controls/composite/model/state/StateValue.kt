package space.kscience.controls.composite.model.state

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A immutable container for a piece of state, coupling the [value] with its metadata,
 * precise time synchronization info, and quality metrics.
 *
 * @param T The type of the value.
 * @property value The actual data.
 * @property originTime The timestamp when the value was generated at the source (e.g., sensor, PLC, or calculation engine).
 *                      This is the "Source Timestamp" in SCADA terminology.
 * @property serverTime The timestamp when the value was received/processed by the system (hub).
 *                      This is the "Server Timestamp" in SCADA terminology.
 * @property quality The reliability assessment of this value.
 */
public data class StateValue<out T>(
    val value: T,
    val originTime: Instant,
    val serverTime: Instant,
    val quality: DataQuality,
) {
    /**
     * Creates a new [StateValue] by applying a mapping function to the value,
     * while preserving timestamps and quality.
     *
     * @param mapper A function to transform the value.
     */
    public fun <R> map(mapper: (T) -> R): StateValue<R> = StateValue(
        value = mapper(value),
        originTime = this.originTime,
        serverTime = this.serverTime,
        quality = this.quality
    )

    /**
     * Timestamp accessor, strictly mapped to [originTime] for backward compatibility semantics.
     */
    public val timestamp: Instant get() = originTime

    public companion object {
        /**
         * A helper function to combine two Quality values, returning the "worst" of the two.
         * BAD > UNCERTAIN > GOOD.
         */
        public fun combineQuality(q1: DataQuality, q2: DataQuality): DataQuality {
            return when {
                q1.status == QualityStatus.BAD || q2.status == QualityStatus.BAD ->
                    DataQuality(QualityStatus.BAD, "COMBINED", "Combined from ${q1.code} and ${q2.code}")
                q1.status == QualityStatus.UNCERTAIN || q2.status == QualityStatus.UNCERTAIN ->
                    DataQuality(QualityStatus.UNCERTAIN, "COMBINED", "Combined from ${q1.code} and ${q2.code}")
                else -> DataQuality.GOOD
            }
        }

        /**
         * Creates a new [StateValue] by combining two source states.
         * The resulting timestamp is the latest of the two, and quality is the worst of the two.
         */
        public fun <T1, T2, R> combine(
            s1: StateValue<T1>,
            s2: StateValue<T2>,
            mapper: (T1, T2) -> R,
        ): StateValue<R> = StateValue(
            value = mapper(s1.value, s2.value),
            originTime = maxOf(s1.originTime, s2.originTime),
            serverTime = maxOf(s1.serverTime, s2.serverTime),
            quality = combineQuality(s1.quality, s2.quality)
        )
    }
}

/**
 * Factory function to create a [StateValue] with [QualityStatus.GOOD] and current time.
 *
 * @param value The value to wrap.
 * @param clock The clock source. Defaults to system clock.
 */
public fun <T> okState(
    value: T,
    clock: Clock = Clock.System
): StateValue<T> {
    val now = clock.now()
    return StateValue(value, now, now, DataQuality.GOOD)
}
