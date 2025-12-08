package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A serializable, declarative descriptor of a Finite State Machine's structure.
 * This serves as a pure data representation for introspection by UIs, documentation tools,
 * and validation engines. It is generated at runtime by introspecting the FSM logic,
 * ensuring it is always synchronized with the actual implementation.
 *
 * @property states A set of unique names for all states in the FSM.
 * @property events A set of fully qualified class names for all events that can drive the FSM.
 * @property initialStateName The name of the initial state of the FSM.
 */
@Serializable
public data class FsmDescriptor(
    val states: Set<String>,
    val events: Set<String>,
    val initialStateName: String,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
