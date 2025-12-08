package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.common.Permission
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.meta.PropertyKind
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.FsmIntegrationSpec
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.controls.composite.model.validation.ValidationRuleDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name

/**
 * A serializable, self-contained descriptor for a device property.
 * This object provides all the static information about a property, making it suitable for
 * introspection, UI generation, and validation without needing a live device instance.
 *
 * @property name The unique, hierarchical name of the property.
 * @property kind The semantic [PropertyKind], classifying the property's nature.
 * @property type The strict [DataType] of the property value.
 * @property structMembers If [type] is [DataType.RECORD], this list describes the fields of the record.
 * @property readable Indicates if the property can be read.
 * @property mutable Indicates if the property can be written to.
 * @property measurement Optional specification for physical units, limits, and scaling.
 * @property transformation Optional specification for deriving this property's value from others or transforming it.
 * @property validation A list of serializable validation rules enforced by the runtime (e.g. Range, Regex).
 * @property clientValidator A [Meta] object containing declarative rules for immediate client-side validation (UI hints).
 * @property metaDescriptor A descriptor for the [Meta] value if the type is [DataType.META].
 *
 * @property readPermissions Permissions required to read this property.
 * @property writePermissions Permissions required to write to this property.
 * @property tags A set of extensible, semantic tags for classification.
 * @property bindings A map of type-safe, protocol-specific configurations.
 * @property policies A unified specification for all operational policies of this member.
 */
@Serializable
public data class PropertyDescriptor(
    override val name: Name,
    public val kind: PropertyKind,
    public val type: DataType,
    public val structMembers: List<PropertyDescriptor> = emptyList(),
    public val readable: Boolean = true,
    public val mutable: Boolean = false,
    public val measurement: MeasurementSpec? = null,
    public val transformation: TransformationSpec? = null,
    public val validation: List<ValidationRuleDescriptor> = emptyList(),
    public val clientValidator: Meta = Meta.EMPTY,
    public val metaDescriptor: MetaDescriptor = MetaDescriptor(),
    // --- MemberSpec Implementation ---
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val policies: MemberPolicies = MemberPolicies(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "property"
    }
}

/**
 * A serializable, self-contained descriptor for a device action.
 *
 * @property name The unique, hierarchical name of the action.
 * @property logic The explicit, type-safe source of the action's executable logic.
 * @property inputMetaDescriptor A descriptor for the action's input [Meta].
 * @property outputDescriptor A descriptor for the action's output [Meta].
 * @property distributable If `true`, a task-based action is a candidate for remote execution.
 * @property taskInputTypeName String representation of the input KType for a Task-based action.
 * @property taskOutputTypeName String representation of the output KType for a Task-based action.
 * @property possibleFaults A set of FQNs of [space.kscience.controls.composite.model.common.DeviceFault]s.
 * @property requiredPredicates A set of names of predicate properties that must be `true`.
 * @property fsm A specification for integrating the action with the device's operational FSM.
 * @property outputAliases A set of user-friendly aliases for output fields.
 *
 * @property readPermissions Permissions required to view this action definition.
 * @property writePermissions Permissions required to execute this action.
 * @property tags A set of extensible, semantic tags.
 * @property bindings A map of type-safe, protocol-specific configurations.
 * @property policies A unified specification for operational policies.
 */
@Serializable
public data class ActionDescriptor(
    override val name: Name,
    public val logic: ActionLogicSource,
    public val inputMetaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val outputDescriptor: MetaDescriptor = MetaDescriptor(),
    public val distributable: Boolean = false,
    public val taskInputTypeName: String? = null,
    public val taskOutputTypeName: String? = null,
    val possibleFaults: Set<String> = emptySet(),
    val requiredPredicates: Set<Name> = emptySet(),
    // --- MemberSpec Implementation ---
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val policies: MemberPolicies = MemberPolicies(),
    // --- Composed Specs ---
    public val fsm: FsmIntegrationSpec? = null,
    public val outputAliases: Set<OutputAlias> = emptySet(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "action"
    }
}
