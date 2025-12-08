package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*

@SerialName("attach")
@Serializable(with = AttachActionSpec.Serializer::class)
public class AttachActionSpec : Scheme(), PlanActionSpec {
    public var deviceAddress: Address by requiredSerializable()
    public var blueprintId: BlueprintId by requiredSerializable()
    public var config: Meta? by convertable(MetaConverter.meta)
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override val description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<AttachActionSpec>(::AttachActionSpec)
    public object Serializer : SchemeAsMetaSerializer<AttachActionSpec>(Companion)
}
