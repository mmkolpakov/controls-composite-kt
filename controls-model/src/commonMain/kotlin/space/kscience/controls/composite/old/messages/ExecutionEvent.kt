package space.kscience.controls.composite.old.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.old.Address
import space.kscience.controls.composite.old.CorrelationId
import space.kscience.controls.composite.old.DeviceFault
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A sealed interface for telemetry events that describe the *process* of an operation's execution,
 * rather than its outcome or a state change. These events are intended for observability,
 * monitoring, and tracing systems (like OpenTelemetry). They provide insights into the
 * lifecycle of an action, cache behavior, and business fault occurrences.
 *
 * All execution events are linked by a [correlationId] to trace a single logical operation
 * across multiple events and devices.
 *
 * @property time The high-precision timestamp when the event occurred.
 * @property sourceDevice The address of the device where the event originated.
 * @property action The name of the action associated with this event.
 * @property correlationId A unique identifier to trace a single logical operation.
 */
@Serializable
public sealed interface ExecutionEvent {
    public val time: Instant
    public val sourceDevice: Address
    public val action: Name
    public val correlationId: CorrelationId?
}

/**
 * An event fired when a request to execute an action is first received and dispatched by the hub.
 * This marks the beginning of the entire operation lifecycle.
 *
 * @param input The input [Meta] provided for the action.
 */
@Serializable
@SerialName("telemetry.action.dispatched")
public data class ActionDispatched(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    public val input: Meta?,
    override val correlationId: CorrelationId?,
) : ExecutionEvent

/**
 * An event fired just before the core logic of an action is executed.
 * This follows `ActionDispatched` and any cache checks.
 */
@Serializable
@SerialName("telemetry.action.started")
public data class ActionStarted(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    override val correlationId: CorrelationId?,
) : ExecutionEvent

/**
 * An event fired immediately after the core logic of an action has successfully completed.
 *
 * @param duration The execution time of the action's logic.
 */
@Serializable
@SerialName("telemetry.action.completed")
public data class ActionCompleted(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    val duration: Duration,
    override val correlationId: CorrelationId?,
) : ExecutionEvent

/**
 * An event fired when a cache lookup for an action's result is successful.
 * This event occurs *instead of* `ActionStarted` and `ActionCompleted`.
 *
 * @param cacheKey The canonical key used for the cache lookup.
 */
@Serializable
@SerialName("telemetry.cache.hit")
public data class CacheHit(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    val cacheKey: String,
    override val correlationId: CorrelationId?,
) : ExecutionEvent

/**
 * An event fired when a cache lookup for an action's result fails, meaning
 * the action's logic will need to be executed. This event is typically followed
 * by `ActionStarted`.
 *
 * @param cacheKey The canonical key used for the cache lookup.
 */
@Serializable
@SerialName("telemetry.cache.miss")
public data class CacheMiss(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    val cacheKey: String,
    override val correlationId: CorrelationId?,
) : ExecutionEvent

/**
 * An event fired when an action completes with a predictable business fault
 * (as opposed to an unexpected system failure).
 *
 * @param fault The structured [DeviceFault] object describing the business error.
 */
@Serializable
@SerialName("telemetry.fault.reported")
public data class FaultReported(
    override val time: Instant,
    override val sourceDevice: Address,
    override val action: Name,
    val fault: DeviceFault,
    override val correlationId: CorrelationId?,
) : ExecutionEvent