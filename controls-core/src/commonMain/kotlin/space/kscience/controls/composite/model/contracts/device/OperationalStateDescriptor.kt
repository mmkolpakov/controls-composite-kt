package space.kscience.controls.composite.model.contracts.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A serializable, platform-agnostic descriptor of a device's operational state.
 * This decouples the [Device] contract from any specific Finite State Machine (FSM) implementation like KStateMachine.
 * It serves as a pure Data Transfer Object (DTO) for observing a device's internal business logic state.
 *
 * @property name The unique name of the state (e.g., "Idle", "Moving", "Acquiring"). This is the primary identifier.
 * @property meta Optional metadata providing additional, structured context about the state. This could include
 *              substate information, progress, or other relevant data that doesn't fit into a simple name.
 */
@Serializable
public data class OperationalStateDescriptor(
    public val name: String,
    public val meta: Meta = Meta.Companion.EMPTY
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
