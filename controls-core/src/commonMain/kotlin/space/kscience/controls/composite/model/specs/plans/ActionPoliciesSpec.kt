package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.policy.TimeoutPolicy
import space.kscience.dataforge.meta.*

/**
 * A container for common execution policies applied to a single action within a plan.
 * This class encapsulates cross-cutting concerns like idempotency, compensation, timeouts, and retries,
 * promoting reuse and separating the action's core logic from its execution strategy.
 */
@Serializable(with = ActionPoliciesSpec.Serializer::class)
public class ActionPoliciesSpec : Scheme() {
    /**
     * An optional unique key to make this action idempotent within a transaction.
     */
    public var key: String? by string()

    /**
     * An optional compensating plan (Saga pattern) to be executed on rollback.
     */
    public var compensation: TransactionPlan? by schemeOrNull(TransactionPlan.Companion)

    /**
     * The policy for handling failures that occur within the compensating plan.
     */
    public var compensationPolicy: CompensationPolicy by enum(CompensationPolicy.ABORT)

    /**
     * An optional policy for timing out this specific action.
     */
    public var timeout: TimeoutPolicy? by schemeOrNull(TimeoutPolicy)

    /**
     * An optional policy for retrying this action if it fails.
     */
    public var retry: RetryPolicy? by schemeOrNull(RetryPolicy)

    public companion object : SchemeSpec<ActionPoliciesSpec>(::ActionPoliciesSpec)
    public object Serializer : SchemeAsMetaSerializer<ActionPoliciesSpec>(Companion)
}
