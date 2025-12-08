package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.scheme

@SerialName("stop")
@Serializable(with = StopActionSpec.Serializer::class)
public class StopActionSpec : Scheme(), PlanActionSpec {
    public var deviceAddress: Address by requiredAddress()
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<StopActionSpec>(::StopActionSpec)
    public object Serializer : SchemeAsMetaSerializer<StopActionSpec>(Companion)
}
