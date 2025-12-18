package space.kscience.controls.composite.old

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.composite.old.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.old.serialization.serializable
import space.kscience.controls.core.spec.RestartStrategy
import space.kscience.dataforge.meta.*
import kotlin.time.Duration.Companion.seconds

/**
 * Defines the policy for restarting a failed device.
 * This class is a [Scheme], allowing for type-safe configuration via DSL and
 * automatic conversion to and from [Meta].
 * It is now serializable and provides structural equality checks.
 *
 * @property maxAttempts The maximum number of restart attempts. `0` means no retries. A value less than 0 means infinite retries.
 * @property strategy The [space.kscience.controls.core.spec.RestartStrategy] to use for calculating delays between retries.
 * @property resetOnSuccess If true, the attempt counter is reset after a successful start.
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

    override fun hashCode(): Int {
        return Meta.hashCode(this.toMeta())
    }

    public companion object : SchemeSpec<RestartPolicy>(::RestartPolicy) {
        public val DEFAULT: RestartPolicy = RestartPolicy()
    }

    /**
     * Custom serializer for RestartPolicy that delegates to Meta serialization.
     */
    public object Serializer : SchemeAsMetaSerializer<RestartPolicy>(RestartPolicy)
}