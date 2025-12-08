package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.enum
import space.kscience.dataforge.meta.scheme

/**
 * A special system-level action that invokes a core lifecycle hook on a device's driver.
 * This makes resource management an explicit, orchestrable step within a lifecycle plan.
 */
@SerialName("driverAction")
@Serializable(with = DriverActionSpec.Serializer::class)
public class DriverActionSpec : Scheme(), PlanActionSpec {
    /**
     * The specific driver action to perform (CONNECT or DISCONNECT).
     */
    public var action: DriverActionType by enum(DriverActionType.CONNECT)

    /**
     * Driver actions do not have standard policies, as they are fundamental lifecycle operations.
     */
    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<DriverActionSpec>(::DriverActionSpec)
    public object Serializer : SchemeAsMetaSerializer<DriverActionSpec>(Companion)
}
