package space.kscience.controls.composite.model.lifecycle

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A serializable, declarative container for the transaction plans that define a device's lifecycle logic.
 * By defining lifecycle stages as plans, the framework enables complex, transactional, and compensatable
 * startup and shutdown sequences.
 *
 * The runtime is responsible for discovering these plans and executing them via a `TransactionCoordinator`
 * at the appropriate lifecycle transition.
 *
 * @property onAttach An optional plan to be executed when the device is attached.
 *                    This plan runs *after* the driver's `onAttach` hook.
 * @property onStart An optional plan that defines the startup sequence.
 * @property onStop An optional plan that defines the shutdown sequence.
 * @property onDetach An optional plan to be executed just before the device is detached.
 *                    This plan runs *before* the driver's `onDetach` hook.
 */
@Serializable
public data class LifecyclePlans(
    val onAttach: TransactionPlan? = null,
    val onStart: TransactionPlan? = null,
    val onStop: TransactionPlan? = null,
    val onDetach: TransactionPlan? = null
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
