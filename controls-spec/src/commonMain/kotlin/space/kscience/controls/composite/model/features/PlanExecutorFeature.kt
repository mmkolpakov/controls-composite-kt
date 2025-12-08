package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta

/**
 * A feature indicating that the device can execute a TransactionPlan.
 * The runtime uses this feature to correctly dispatch plan-based actions to the device.
 */
@Serializable
@SerialName("feature.planExecutor")
public data class PlanExecutorFeature(
    override val capability: String = "space.kscience.controls.composite.model.contracts.device.PlanExecutorDevice"
) : Feature {
    override fun toMeta(): Meta = Meta {
        "capability" put capability
    }
}
