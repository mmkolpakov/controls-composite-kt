package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.scheme

@SerialName("sequence")
@Serializable(with = SequenceActionSpec.Serializer::class)
public class SequenceActionSpec : Scheme(), PlanActionSpec {
    public var actions: List<PlanActionSpec> by listOfConvertable(PolymorphicSerializer(PlanActionSpec::class))
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<SequenceActionSpec>(::SequenceActionSpec)
    public object Serializer : SchemeAsMetaSerializer<SequenceActionSpec>(Companion)
}
