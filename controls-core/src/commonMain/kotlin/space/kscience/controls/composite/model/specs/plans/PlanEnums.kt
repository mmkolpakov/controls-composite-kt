package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.Serializable

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
