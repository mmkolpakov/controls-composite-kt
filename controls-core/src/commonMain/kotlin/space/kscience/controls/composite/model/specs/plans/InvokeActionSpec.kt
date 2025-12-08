package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * An action that invokes another action on a specified device.
 * This is the primary mechanism for orchestrating device behaviors within a plan.
 */
@SerialName("invoke")
@Serializable(with = InvokeActionSpec.Serializer::class)
public class InvokeActionSpec : Scheme(), PlanActionSpec {
    /**
     * The network-wide address of the target device.
     */
    public var deviceAddress: Address by requiredAddress()

    /**
     * The name of the action to invoke on the target device.
     */
    public var actionName: Name by requiredName()

    /**
     * An optional [ComputableValue] representing the input arguments for the action.
     * The runtime is responsible for resolving this value before execution.
     */
    public var input: ComputableValue? by convertable(MetaConverter.serializable(ComputableValue.serializer()))

    /**
     * An optional name for a variable in the plan's execution context. If provided,
     * the result of this action will be stored in this variable, making it available
     * to subsequent actions in the plan.
     */
    public var resultVariable: Name? by convertable(MetaConverter.serializable(Name.serializer()))

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<InvokeActionSpec>(::InvokeActionSpec)
    public object Serializer : SchemeAsMetaSerializer<InvokeActionSpec>(Companion)
}
