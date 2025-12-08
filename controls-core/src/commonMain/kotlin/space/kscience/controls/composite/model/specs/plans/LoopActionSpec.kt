package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * An action that iterates over a collection of items (previously stored in the plan's execution context)
 * and executes a sub-plan for each item.
 */
@SerialName("loop")
@Serializable(with = LoopActionSpec.Serializer::class)
public class LoopActionSpec : Scheme(), PlanActionSpec {
    /**
     * The name under which the collection of items is stored in the `PlanExecutionContext`.
     */
    public var iterableName: Name by requiredName()

    /**
     * The name of the variable that will hold the current item within the sub-plan's scope.
     */
    public var loopVariableName: String by requiredString()

    /**
     * The [TransactionPlan] to be executed for each item in the collection.
     */
    public var body: TransactionPlan by scheme(TransactionPlan.Companion)

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<LoopActionSpec>(::LoopActionSpec)
    public object Serializer : SchemeAsMetaSerializer<LoopActionSpec>(Companion)
}
