package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A declarative action to acknowledge an alarm.
 */
@SerialName("alarm.acknowledge")
@Serializable(with = AcknowledgeAlarmActionSpec.Serializer::class)
public class AcknowledgeAlarmActionSpec : Scheme(), PlanActionSpec {
    /**
     * The name of the alarm to acknowledge.
     */
    public var alarmName: Name by requiredName()

    override val description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    /**
     * An optional, computable value for an acknowledgement comment.
     */
    public var comment: ComputableValue? by convertable(MetaConverter.serializable(ComputableValue.serializer()))

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)

    public companion object : SchemeSpec<AcknowledgeAlarmActionSpec>(::AcknowledgeAlarmActionSpec)
    public object Serializer : SchemeAsMetaSerializer<AcknowledgeAlarmActionSpec>(Companion)
}
