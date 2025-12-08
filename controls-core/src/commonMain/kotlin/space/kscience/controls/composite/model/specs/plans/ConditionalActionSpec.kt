package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.scheme
import space.kscience.dataforge.meta.schemeOrNull

/**
 * A conditional action that executes one of two branches (`thenPlan` or `elsePlan`) based on the evaluation
 * of a predicate.
 */
@SerialName("condition")
@Serializable(with = ConditionalActionSpec.Serializer::class)
public class ConditionalActionSpec : Scheme(), PlanActionSpec {
    /**
     * The [PredicateSpec] describing the condition to evaluate.
     */
    public var predicate: PredicateSpec by scheme(PredicateSpec)

    /**
     * The [TransactionPlan] to execute if the predicate is true.
     */
    public var thenPlan: TransactionPlan by scheme(TransactionPlan.Companion)

    /**
     * An optional [TransactionPlan] to execute if the predicate is false.
     */
    public var elsePlan: TransactionPlan? by schemeOrNull(TransactionPlan.Companion)

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<ConditionalActionSpec>(::ConditionalActionSpec)
    public object Serializer : SchemeAsMetaSerializer<ConditionalActionSpec>(Companion)
}
