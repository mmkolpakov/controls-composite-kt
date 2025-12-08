package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.FsmDescriptor
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A feature indicating that the device has an operational Finite State Machine (FSM).
 *
 * @property descriptor The introspected structure of the operational FSM.
 */
@Serializable
@SerialName("feature.operationalFsm")
public data class OperationalFsmFeature(
    val descriptor: FsmDescriptor,
) : Feature {
    override val capability: String = CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val CAPABILITY: String = "ru.nsk.kstatemachine.statemachine.StateMachine"
    }
}
