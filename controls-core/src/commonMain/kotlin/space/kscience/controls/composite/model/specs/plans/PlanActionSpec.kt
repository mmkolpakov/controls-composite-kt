package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.Polymorphic
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.dataforge.meta.MetaRepr

/**
 * A serializable, type-safe representation of a single action or a composite block of actions within a transaction plan.
 * The runtime ([space.kscience.controls.composite.model.services.TransactionCoordinator]) is responsible for interpreting and executing these specifications.
 */
@Polymorphic
public interface PlanActionSpec : MetaRepr {
    /**
     * The execution policies for this action, including idempotency, compensation, and resilience.
     */
    public val policies: ActionPoliciesSpec

    /**
     * A human-readable description of this step in the plan.
     * Used for UI progress reporting and audit logs.
     *
     * Example: "Warming up the oven to 200C".
     */
    public val description: LocalizedText?
}
