package space.kscience.controls.composite.model.lifecycle

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.specs.policy.RestartPolicy
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A configuration for a device's lifecycle management within a hub.
 *
 * While this class provides type-safe accessors, in a declarative blueprint (`.kts` file), any of its properties
 * can be assigned a string template for dynamic resolution at runtime.
 *
 * @property lifecycleMode Defines how the device's lifecycle is tied to its parent.
 * @property onError The strategy to apply when this device fails as a child of another device.
 * @property lazyAttach If true, the device will be instantiated only on first access.
 * @property persistence Configuration for saving and restoring the device's state.
 * @property startTimeout The maximum duration to wait for the 'start' transition to complete.
 * @property startRetry An optional policy for retrying a failed 'start' transition. Use [effectiveStartRetry] for runtime logic.
 * @property stopTimeout The maximum duration to wait for the 'stop' transition to complete.
 * @property restartPolicy The policy for automatically restarting this device upon entering a 'Failed' lifecycle state.
 *                         This is distinct from `RetryPolicy`, which applies to transient operation failures.
 */
@Serializable(with = DeviceLifecycleConfig.Serializer::class)
public class DeviceLifecycleConfig : Scheme() {
    public var lifecycleMode: LifecycleMode by enum(LifecycleMode.LINKED)
    public var onError: ChildDeviceErrorHandler by enum(ChildDeviceErrorHandler.RESTART)
    public var lazyAttach: Boolean by boolean(false)
    public var persistence: PersistenceConfig by scheme(PersistenceConfig)

    public var startTimeout: Duration by duration(30.seconds)

    /**
     * The retry policy configuration for the start operation.
     * If null, the system default should be used.
     */
    public var startRetry: RetryPolicy? by schemeOrNull(RetryPolicy)

    public var stopTimeout: Duration by duration(10.seconds)

    public var restartPolicy: RestartPolicy by scheme(RestartPolicy)

    /**
     * Provides the effective retry policy for starting the device.
     * If [startRetry] is not explicitly configured in the meta, this returns a default policy
     * of 3 attempts.
     */
    public val effectiveStartRetry: RetryPolicy
        get() = startRetry ?: RetryPolicy {
            maxAttempts = 3
        }

    public companion object : SchemeSpec<DeviceLifecycleConfig>(::DeviceLifecycleConfig) {
        /**
         * The default, immutable configuration for a device lifecycle.
         */
        public val DEFAULT: DeviceLifecycleConfig by lazy { DeviceLifecycleConfig() }
    }

    public object Serializer : SchemeAsMetaSerializer<DeviceLifecycleConfig>(Companion)
}
