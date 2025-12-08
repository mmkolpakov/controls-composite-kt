package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.scheme
import kotlin.time.Duration

/**
 * An action that introduces a delay in the execution of a plan.
 * This action does not have meaningful compensation, timeout, or retry policies, so its policies are empty by default.
 */
@SerialName("delay")
@Serializable(with = DelayActionSpec.Serializer::class)
public class DelayActionSpec : Scheme(), PlanActionSpec {
    /**
     * The duration of the delay.
     */
    public var duration: Duration by duration(Duration.ZERO)

    /**
     * Policies for this action. By design, delay actions do not support standard execution policies.
     */
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<DelayActionSpec>(::DelayActionSpec)
    public object Serializer : SchemeAsMetaSerializer<DelayActionSpec>(Companion)
}
