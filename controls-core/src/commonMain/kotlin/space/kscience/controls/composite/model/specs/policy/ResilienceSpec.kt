package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A policy for retrying a single, failed, transient operation (like a remote API call or a failed start attempt).
 * This is distinct from [RestartPolicy], which handles the recovery of a device from a persistent 'Failed' lifecycle state.
 *
 * @property maxAttempts The maximum number of retry attempts. `0` means no retries. A value less than 0 means infinite retries.
 * @property strategy The [RestartStrategy] to use for calculating delays between retries.
 */
@Serializable(with = RetryPolicy.Serializer::class)
public class RetryPolicy : Scheme() {
    public var maxAttempts: Int by int(3)
    public var strategy: RestartStrategy by convertable(
        MetaConverter.serializable(RestartStrategy.serializer()),
        default = RestartStrategy.Linear(1.seconds)
    )

    public companion object : SchemeSpec<RetryPolicy>(::RetryPolicy)
    public object Serializer : SchemeAsMetaSerializer<RetryPolicy>(Companion)
}

/**
 * A policy for defining a timeout for a single, specific operation.
 *
 * @property duration The maximum duration to wait for an operation to complete.
 */
@Serializable(with = TimeoutPolicy.Serializer::class)
public class TimeoutPolicy : Scheme() {
    public var duration: Duration by duration(30.seconds)

    public companion object : SchemeSpec<TimeoutPolicy>(::TimeoutPolicy)
    public object Serializer : SchemeAsMetaSerializer<TimeoutPolicy>(Companion)
}


/**
 * A container for resilience policies for peer-to-peer communication or other remote operations.
 * This is primarily intended for configuring connections defined in [PeerBlueprint].
 *
 * @property retry An optional policy for retrying failed operations.
 * @property timeout An optional policy for timing out long-running operations.
 */
@Serializable(with = ResiliencePolicy.Serializer::class)
public class ResiliencePolicy : Scheme() {
    public var retry: RetryPolicy? by schemeOrNull(RetryPolicy)
    public var timeout: TimeoutPolicy? by schemeOrNull(TimeoutPolicy)

    public companion object : SchemeSpec<ResiliencePolicy>(::ResiliencePolicy)
    public object Serializer : SchemeAsMetaSerializer<ResiliencePolicy>(Companion)
}
