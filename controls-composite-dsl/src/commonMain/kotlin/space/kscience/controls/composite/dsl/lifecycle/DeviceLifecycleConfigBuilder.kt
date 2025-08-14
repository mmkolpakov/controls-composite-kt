package space.kscience.controls.composite.dsl.lifecycle

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import space.kscience.controls.composite.model.*
import space.kscience.controls.composite.model.lifecycle.ChildDeviceErrorHandler
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleConfig
import space.kscience.controls.composite.model.lifecycle.LifecycleMode
import space.kscience.controls.composite.model.lifecycle.PersistenceConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A DSL marker for building device lifecycle configurations.
 */
@DslMarker
public annotation class LifecycleDsl

/**
 * A DSL builder for configuring a [DeviceLifecycleConfig].
 * This builder is used both for top-level device attachment and for child device configuration within a spec.
 */
@LifecycleDsl
public class DeviceLifecycleConfigBuilder {
    /**
     * The lifecycle mode of the device. Defaults to [LifecycleMode.LINKED].
     * This is primarily relevant for child devices.
     */
    public var lifecycleMode: LifecycleMode = LifecycleMode.LINKED

    /**
     * The timeout for the device's start operation. Defaults to 30 seconds.
     */
    public var startTimeout: Duration? = 30.seconds

    /**
     * The timeout for acknowledging a lifecycle operation.
     */
    public var ackTimeout: Duration = 10.seconds

    /**
     * The capacity of the actor's command channel. Null means `UNLIMITED`.
     */
    public var actorChannelCapacity: Int? = null

    /**
     * A delay to wait before starting the device. Defaults to [Duration.ZERO].
     */
    public var startDelay: Duration = Duration.ZERO

    /**
     * The timeout for the device's stop operation. Defaults to 10 seconds.
     */
    public var stopTimeout: Duration? = 10.seconds

    /**
     * If true, the device will be instantiated lazily on first access. Defaults to false.
     * This is primarily relevant for child devices.
     */
    public var lazyAttach: Boolean = false

    /**
     * The strategy for handling errors in the device if it's a child. Defaults to [ChildDeviceErrorHandler.RESTART].
     */
    public var onError: ChildDeviceErrorHandler = ChildDeviceErrorHandler.RESTART

    /**
     * The policy for restarting the device upon failure.
     */
    public var restartPolicy: RestartPolicy = RestartPolicy()

    /**
     * An optional custom CoroutineScope for the device. If null, a new scope will be created.
     */
    public var coroutineScope: CoroutineScope? = null

    /**
     * An optional CoroutineDispatcher for the device's scope. If null, the manager's default is used.
     */
    public var dispatcher: CoroutineDispatcher? = null

    public var persistence: PersistenceConfig = PersistenceConfig()

    public fun persistence(block: PersistenceConfig.() -> Unit) {
        this.persistence = PersistenceConfig().apply(block)
    }

    /**
     * Builds the immutable [DeviceLifecycleConfig] object.
     */
    internal fun build(): DeviceLifecycleConfig = DeviceLifecycleConfig(
        lifecycleMode = lifecycleMode,
        startTimeout = startTimeout,
        stopTimeout = stopTimeout,
        onError = onError,
        restartPolicy = restartPolicy,
        lazyAttach = lazyAttach,
        persistence = persistence
    )
}