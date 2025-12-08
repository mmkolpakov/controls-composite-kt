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
import space.kscience.dataforge.meta.enum
import space.kscience.dataforge.meta.scheme

@SerialName("parallel")
@Serializable(with = ParallelActionSpec.Serializer::class)
public class ParallelActionSpec : Scheme(), PlanActionSpec {
    public var actions: List<PlanActionSpec> by listOfConvertable(PolymorphicSerializer(PlanActionSpec::class))
    public var failureStrategy: ParallelFailureStrategy by enum(ParallelFailureStrategy.FAIL_FAST)
    public var compensationOrder: CompensationOrder by enum(CompensationOrder.SEQUENTIAL_REVERSE)
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<ParallelActionSpec>(::ParallelActionSpec)
    public object Serializer : SchemeAsMetaSerializer<ParallelActionSpec>(Companion)
}
