package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * An action that pauses plan execution until a specific boolean property (a predicate)
 * on a device becomes `true`.
 */
@SerialName("awaitPredicate")
@Serializable(with = AwaitPredicateActionSpec.Serializer::class)
public class AwaitPredicateActionSpec : Scheme(), PlanActionSpec {
    /**
     * The network-wide address of the device to monitor.
     */
    public var deviceAddress: Address by requiredAddress()

    /**
     * The name of the boolean property (must be of kind `PREDICATE`).
     */
    public var predicateName: Name by requiredName()

    /**
     * An optional maximum duration to wait for the predicate to become true.
     * If the timeout is exceeded, the action fails, triggering a rollback.
     */
    public var awaitTimeout: Duration? by convertable(MetaConverter.duration)

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<AwaitPredicateActionSpec>(::AwaitPredicateActionSpec)
    public object Serializer : SchemeAsMetaSerializer<AwaitPredicateActionSpec>(Companion)
}
