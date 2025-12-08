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
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A declarative action to temporarily suppress (shelve) an alarm.
 */
@SerialName("alarm.shelve")
@Serializable(with = ShelveAlarmActionSpec.Serializer::class)
public class ShelveAlarmActionSpec : Scheme(), PlanActionSpec {
    /**
     * The name of the alarm to shelve.
     */
    public var alarmName: Name by requiredName()

    /**
     * The duration for which the alarm should be shelved.
     */
    public var duration: ComputableValue by requiredSerializable()

    /**
     * An optional comment.
     */
    public var comment: ComputableValue? by convertable(MetaConverter.serializable(ComputableValue.serializer()))

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<ShelveAlarmActionSpec>(::ShelveAlarmActionSpec)
    public object Serializer : SchemeAsMetaSerializer<ShelveAlarmActionSpec>(Companion)
}
