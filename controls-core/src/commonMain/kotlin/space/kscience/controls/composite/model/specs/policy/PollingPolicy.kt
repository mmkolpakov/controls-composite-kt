package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.enum
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Defines the behavior of the data polling mechanism (Scan Class).
 */
@Serializable
public enum class PollingMode {
    /**
     * Poll continuously at the specified interval, regardless of whether anyone is subscribed.
     * Useful for Historian/Logging data.
     */
    ALWAYS,

    /**
     * Poll only when there is at least one active subscriber (UI or Logic).
     * Stops polling when the last subscriber disconnects. Reduces bus load.
     */
    ON_DEMAND,

    /**
     * Do not poll automatically. Value is updated only by explicit read requests or push notifications.
     */
    MANUAL
}

/**
 * A policy configuration for data acquisition frequency and strategy.
 * Drivers use this to group tags into efficient "Scan Groups".
 *
 * @property interval The interval between poll attempts.
 * @property mode The polling strategy.
 */
@Serializable(with = PollingPolicy.Serializer::class)
public class PollingPolicy : Scheme() {
    public var interval: Duration by duration(1.seconds)
    public var mode: PollingMode by enum(PollingMode.ON_DEMAND)

    public companion object : SchemeSpec<PollingPolicy>(::PollingPolicy)
    public object Serializer : SchemeAsMetaSerializer<PollingPolicy>(Companion)
}
