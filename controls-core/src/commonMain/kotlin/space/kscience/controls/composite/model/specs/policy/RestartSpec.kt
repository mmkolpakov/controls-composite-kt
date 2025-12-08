package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Defines a strategy for calculating the delay between restart attempts for a failed device.
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
 * Defines the policy for automatically restarting a device that has entered a 'Failed' lifecycle state.
 * This is distinct from [RetryPolicy], which applies to transient operation failures.
 *
 * @property maxAttempts The maximum number of restart attempts. `0` means no restarts. A value less than 0 means infinite restarts.
 * @property strategy The [RestartStrategy] to use for calculating delays between restarts.
 * @property resetOnSuccess If true, the attempt counter is reset after a successful restart and subsequent stable operation.
 */
@Serializable(with = RestartPolicy.Serializer::class)
public class RestartPolicy : Scheme() {
    public var maxAttempts: Int by int(5)
    public var strategy: RestartStrategy by convertable(
        MetaConverter.serializable(RestartStrategy.serializer()),
        default = RestartStrategy.Linear(2.seconds)
    )
    public var resetOnSuccess: Boolean by boolean(true)


    /**
     * The effective number of attempts to be used by a runtime.
     * Treats non-positive `maxAttempts` as [Int.MAX_VALUE] to represent infinity in a loop-safe manner.
     */
    @Transient
    public val effectiveMaxAttempts: Int
        get() = if (maxAttempts <= 0) Int.MAX_VALUE else maxAttempts

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherMeta = (other as? MetaRepr)?.toMeta() ?: return false
        return Meta.equals(this.toMeta(), otherMeta)
    }

    override fun hashCode(): Int = Meta.hashCode(this.toMeta())

    public companion object : SchemeSpec<RestartPolicy>(::RestartPolicy) {
        public val DEFAULT: RestartPolicy = RestartPolicy()
    }
    public object Serializer : SchemeAsMetaSerializer<RestartPolicy>(Companion)
}
