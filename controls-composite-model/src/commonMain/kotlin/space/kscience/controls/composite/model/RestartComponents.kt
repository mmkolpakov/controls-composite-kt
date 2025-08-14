package space.kscience.controls.composite.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Defines a strategy for calculating the delay between restart attempts.
 */
@Serializable
public sealed interface RestartStrategy : MetaRepr {

    @Serializable
    @SerialName("linear")
    public data class Linear(val baseDelay: Duration) : RestartStrategy {
        override fun toMeta(): Meta = serializableToMeta(serializer(), this)
    }

    @Serializable
    @SerialName("exponential")
    public data class ExponentialBackoff(val baseDelay: Duration) : RestartStrategy {
        override fun toMeta(): Meta = serializableToMeta(serializer(), this)
    }

    @Serializable
    @SerialName("fibonacci")
    public data class Fibonacci(val baseDelay: Duration) : RestartStrategy {
        override fun toMeta(): Meta = serializableToMeta(serializer(), this)
    }
}

/**
 * Defines the policy for restarting a failed device.
 *
 * @property maxAttempts The maximum number of restart attempts before giving up. A value <= 0 means infinite attempts.
 * @property strategy The [RestartStrategy] to use for calculating delays.
 * @property resetOnSuccess If true, the attempt counter is reset after a successful start.
 */
@Serializable
public data class RestartPolicy(
    val maxAttempts: Int = 5,
    val strategy: RestartStrategy = RestartStrategy.Linear(2.seconds),
    val resetOnSuccess: Boolean = true,
) : MetaRepr {
    /**
     * The effective number of attempts to be used by a runtime.
     * Treats non-positive `maxAttempts` as [Int.MAX_VALUE] to represent infinity in a loop-safe manner.
     */
    @Transient
    public val effectiveMaxAttempts: Int = if (maxAttempts <= 0) Int.MAX_VALUE else maxAttempts

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public val DEFAULT: RestartPolicy = RestartPolicy()
    }
}