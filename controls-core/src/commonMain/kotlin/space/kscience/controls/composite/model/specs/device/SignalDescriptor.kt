package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Permission
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name

/**
 * A serializable, declarative specification for a "signal" that can be sent to a device.
 * A signal is a lightweight, fire-and-forget command that typically triggers an event in the
 * device's operational Finite State Machine (FSM).
 *
 * Signals differ from actions in that they do not return a result and are primarily used
 * for state transitions (e.g., "Stop", "Pause", "Reset").
 *
 * @property name The unique name of the signal.
 * @property eventTypeName The fully qualified serial name of the event class to be posted when the signal is received.
 * @property inputDescriptor A descriptor for the [Meta] payload that can accompany the signal.
 * @property readPermissions Permissions required to read/introspect this signal definition.
 * @property writePermissions Permissions required to trigger (write) this signal.
 * @property tags A set of extensible, semantic tags for classification.
 * @property bindings A map of type-safe, protocol-specific configurations.
 * @property policies A unified specification for all operational policies of this signal.
 */
@Serializable
public data class SignalDescriptor(
    override val name: Name,
    val eventTypeName: String,
    val inputDescriptor: MetaDescriptor = MetaDescriptor.EMPTY,
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val policies: MemberPolicies = MemberPolicies(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "signal"
    }
}
