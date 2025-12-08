package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.instant
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import kotlin.time.Instant

/**
 * A container for a sequence of actions to be executed transactionally.
 * The plan consists of a single root [PlanActionSpec], which can be a composite action (sequence or parallel).
 */
@Serializable(with = TransactionPlan.Serializer::class)
public class TransactionPlan : Scheme() {
    /**
     * The root action of the plan. This property is mandatory. If the plan contains multiple top-level steps,
     * they should be wrapped in a [SequenceActionSpec] or [ParallelActionSpec].
     */
    public var rootAction: PlanActionSpec by requiredConvertable(
        MetaConverter.serializable(PolymorphicSerializer(PlanActionSpec::class))
    )

    /**
     * An optional absolute time by which the entire transaction must complete. If the deadline is
     * exceeded, the transaction is considered failed, and a rollback is initiated. This provides
     * a global timeout for the whole operation.
     */
    public var deadline: Instant? by convertable(MetaConverter.instant)


    public companion object : SchemeSpec<TransactionPlan>(::TransactionPlan)
    public object Serializer : SchemeAsMetaSerializer<TransactionPlan>(Companion)
}
