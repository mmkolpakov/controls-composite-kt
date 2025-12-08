package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.scheme
import space.kscience.dataforge.names.Name

/**
 * An action that updates the value of an existing variable in the plan's execution context.
 * The runtime will search for the variable in the current scope and its parent scopes.
 */
@SerialName("set")
@Serializable(with = SetVariableActionSpec.Serializer::class)
public class SetVariableActionSpec : Scheme(), PlanActionSpec {
    /**
     * The name of the variable to update.
     */
    public var variableName: Name by requiredName()

    /**
     * The new value for the variable.
     */
    public var value: ComputableValue by requiredSerializable()

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<SetVariableActionSpec>(::SetVariableActionSpec)
    public object Serializer : SchemeAsMetaSerializer<SetVariableActionSpec>(Companion)
}
