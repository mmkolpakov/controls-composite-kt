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
 * An action that declares a new variable in the current scope of the plan's execution context.
 */
@SerialName("declare")
@Serializable(with = DeclareVariableActionSpec.Serializer::class)
public class DeclareVariableActionSpec : Scheme(), PlanActionSpec {
    /**
     * The name of the new variable.
     */
    public var variableName: Name by requiredName()

    /**
     * The initial value for the variable, which will be resolved by the runtime.
     */
    public var value: ComputableValue by requiredSerializable()

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<DeclareVariableActionSpec>(::DeclareVariableActionSpec)
    public object Serializer : SchemeAsMetaSerializer<DeclareVariableActionSpec>(Companion)
}
