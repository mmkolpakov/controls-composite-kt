package space.kscience.controls.automation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.features.Feature
import space.kscience.dataforge.meta.Meta

/**
 * A feature indicating that the device can execute a [TransactionPlan].
 * The runtime uses this feature to correctly dispatch plan-based actions to the device.
 */
@Serializable
@SerialName("feature.planExecutor")
public data class PlanExecutorFeature(
    override val capability: String = PlanExecutorDevice.CAPABILITY
) : Feature {
    override fun toMeta(): Meta = Meta {
        "capability" put capability
    }
}