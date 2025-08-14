package space.kscience.controls.composite.model.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.Address
import space.kscience.controls.composite.model.RetryPolicy
import space.kscience.controls.composite.model.TimeoutPolicy
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * Defines the strategy for handling failures within a [ParallelActionSpec].
 */
@Serializable
public enum class ParallelFailureStrategy {
    /** If any action fails, immediately cancel all other running actions and fail the block. */
    FAIL_FAST,

    /** Wait for all actions to complete, regardless of individual failures, then fail if any action failed. */
    COLLECT_ALL,

    /** Complete successfully even if some actions fail, as long as at least one succeeds (best-effort). */
    BEST_EFFORT,
}

/**
 * Defines the policy for handling a failure that occurs within a compensating action.
 */
@Serializable
public enum class CompensationPolicy {
    /** Abort the entire rollback process. The system may be left in an inconsistent state. Requires manual intervention. */
    ABORT,

    /** Continue the rollback process with the remaining compensating actions, but flag the transaction as requiring attention. */
    CONTINUE_AND_FLAG,

    /** Retry the failed compensating action according to its own retry policy, if defined. */
    RETRY,
}

/**
 * Defines the execution order for compensating actions within a [ParallelActionSpec].
 * This is crucial for correctly rolling back parallel operations that may have dependencies.
 */
@Serializable
public enum class CompensationOrder {
    /**
     * Execute compensating actions in the reverse order of their corresponding forward actions, one by one.
     * This is the safest default for most scenarios, as it unwinds the operation stack.
     */
    SEQUENTIAL_REVERSE,

    /**
     * Execute all compensating actions in parallel.
     * Use with caution, as it may lead to race conditions if compensations depend on each other.
     */
    PARALLEL
}


/**
 * A serializable, type-safe representation of a single action or a composite block of actions within a transaction plan.
 * The runtime ([space.kscience.controls.composite.model.services.TransactionCoordinator]) is responsible for interpreting and executing these specifications.
 *
 * @property idempotencyKey An optional unique key to make this action idempotent. The runtime should track executed keys
 *                          within the scope of a transaction to prevent duplicate operations on retry or replay.
 *                          The key's uniqueness guarantee is the responsibility of the plan creator.
 * @property compensation An optional compensating plan (Saga pattern) to be executed on rollback if this action
 *                        succeeded but a later action in the sequence failed.
 * @property compensationPolicy The policy for handling failures that occur *within* the compensating plan itself.
 * @property timeout An optional policy for timing out this specific action. If the action exceeds the timeout, it is
 *                   considered failed and will trigger a rollback.
 * @property retry An optional policy for retrying this action if it fails.
 */
@Serializable
public sealed interface ActionSpec : MetaRepr {
    public val idempotencyKey: String?
    public val compensation: TransactionPlan?
    public val compensationPolicy: CompensationPolicy
    public val timeout: TimeoutPolicy?
    public val retry: RetryPolicy?
}

@Serializable
@SerialName("parallel")
public data class ParallelActionSpec(
    val actions: List<ActionSpec>,
    val failureStrategy: ParallelFailureStrategy = ParallelFailureStrategy.FAIL_FAST,
    val compensationOrder: CompensationOrder = CompensationOrder.SEQUENTIAL_REVERSE,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("attach")
public data class AttachActionSpec(
    val deviceAddress: Address,
    val blueprintId: BlueprintId,
    val config: Meta = Meta.EMPTY,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("detach")
public data class DetachActionSpec(
    val deviceAddress: Address,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("start")
public data class StartActionSpec(
    val deviceAddress: Address,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("stop")
public data class StopActionSpec(
    val deviceAddress: Address,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("write")
public data class WritePropertyActionSpec(
    val deviceAddress: Address,
    val propertyName: Name,
    val value: Meta,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

@Serializable
@SerialName("sequence")
public data class SequenceActionSpec(
    val actions: List<ActionSpec>,
    override val idempotencyKey: String? = null,
    override val compensation: TransactionPlan? = null,
    override val compensationPolicy: CompensationPolicy = CompensationPolicy.ABORT,
    override val timeout: TimeoutPolicy? = null,
    override val retry: RetryPolicy? = null,
) : ActionSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}


/**
 * A container for a sequence of actions to be executed transactionally.
 * The plan consists of a single root [ActionSpec], which can be a composite action (sequence or parallel).
 *
 * @property rootAction The root action of the plan. If the plan contains multiple top-level steps,
 *                      they should be wrapped in a [SequenceActionSpec] or [ParallelActionSpec].
 * @property deadline An optional absolute time by which the entire transaction must complete. If the deadline is
 *                    exceeded, the transaction is considered failed, and a rollback is initiated. This provides
 *                    a global timeout for the whole operation.
 */
@Serializable
public data class TransactionPlan(
    val rootAction: ActionSpec,
    val deadline: Instant? = null
) : MetaRepr {
    override fun toMeta(): Meta = rootAction.toMeta()
}