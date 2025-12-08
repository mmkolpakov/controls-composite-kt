package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import kotlin.time.Duration

/**
 * An action that pauses plan execution until an external signal is received from an operator or another system.
 */
@SerialName("awaitSignal")
@Serializable(with = AwaitSignalActionSpec.Serializer::class)
public class AwaitSignalActionSpec : Scheme(), PlanActionSpec {
    /**
     * A unique identifier for the expected signal. The runtime uses this to correlate the waiting plan
     * with an incoming signal.
     */
    public var signalId: String by requiredString()

    /**
     * An optional human-readable message to be displayed to an operator, explaining what is expected.
     */
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    /**
     * An optional maximum duration to wait for the signal. If no signal is received within this
     * time, the action fails, and the plan is rolled back.
     */
    public var signalTimeout: Duration? by convertable(MetaConverter.duration)

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)

    public companion object : SchemeSpec<AwaitSignalActionSpec>(::AwaitSignalActionSpec)
    public object Serializer : SchemeAsMetaSerializer<AwaitSignalActionSpec>(Companion)
}
