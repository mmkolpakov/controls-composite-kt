package space.kscience.controls.core.descriptors

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.core.identifiers.Permission
import space.kscience.controls.core.meta.AdapterBinding
import space.kscience.controls.core.meta.MemberTag
import space.kscience.controls.core.descriptors.PropertyKind
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.controls.core.spec.ResourceLockSpec
import space.kscience.controls.core.validation.ValidationRuleDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.Value
import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.allowedValues
import space.kscience.dataforge.names.Name
import kotlin.getValue
import kotlin.reflect.KType
import kotlin.time.Duration

/**
 * A serializable, self-contained descriptor for a device property. This object provides all the static information
 * about a property, making it suitable for introspection, UI generation, and validation without needing a live
 * device instance.
 *
 * @property name The unique, potentially hierarchical name of the property. Uses [Name] for consistency with DataForge.
 * @property kind The semantic [PropertyKind], classifying the property's nature (e.g., physical, logical).
 * @property valueTypeName The string representation of the property's [KType]. Essential for runtime type validation
 *                         in dynamic environments without reflection.
 * @property description An optional human-readable description of the property.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property group An optional string for grouping related properties in a user interface.
 * @property icon An optional string identifier for a visual icon, used by UIs.
 * @property timeout The default timeout for both read and write operations on this property. The runtime is expected
 *                   to enforce this constraint. If null, a system-wide default may be used.
 * @property requiredLocks A list of resource locks that must be acquired by the runtime before this property can be
 *                         accessed. This is used to prevent concurrent access to shared resources.
 * @property metaDescriptor A descriptor for the [Meta] value of the property, defining its structure and constraints.
 * @property readable Indicates if the property can be read. Defaults to `true`.
 * @property mutable Indicates if the property can be written to. Defaults to `false`.
 * @property permissions The set of permissions required to access (read or write) this property.
 * @property metrics A [Meta] block for configuring metrics collection for this property (e.g., enabling counters, gauges).
 * @property labels A map of static labels to be attached to any metrics generated for this property.
 * @property validationRules A list of declarative, serializable validation rules to be enforced on this property.
 * @property persistent Indicates if this property's state is required to be included in device snapshots and restored
 *                      when the device starts if persistence is enabled.
 * @property transient Indicates if this property's state must NOT be included in device snapshots. Overrides [persistent] if both are set.
 * @property unit An optional string representing the physical unit of the property's value (e.g., "volts", "mm", "deg").
 * @property valueRange An optional range constraint for numeric properties, useful for UI widgets like sliders.
 * @property widgetHint A hint for UI generators suggesting a preferred widget type (e.g., "slider", "checkbox").
 * @property tags A set of extensible, semantic [MemberTag]s for classification by external systems (UI, documentation, etc.).
 * @property bindings A map of type-safe, protocol-specific configurations. The key is a unique string
 *                    identifying the protocol adapter (e.g., "modbus", "yandex"), and the value is the
 *                    serializable [AdapterBinding] configuration.
 */
@Serializable
public data class PropertyDescriptor(
    public override val name: Name,
    public val kind: PropertyKind,
    public val valueTypeName: String,
    public val description: String? = null,
    public val help: String? = null,
    public val group: String? = null,
    public val icon: String? = null,
    public val timeout: @Contextual Duration? = null,
    public val requiredLocks: List<ResourceLockSpec> = emptyList(),
    public val metaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val readable: Boolean = true,
    public val mutable: Boolean = false,
    public val permissions: Set<Permission> = emptySet(),
    public val metrics: Meta = Meta.EMPTY,
    public val labels: Map<String, String> = emptyMap(),
    public val validationRules: List<ValidationRuleDescriptor> = emptyList(),
    public val persistent: Boolean = false,
    public val transient: Boolean = false,
    public val unit: String? = null,
    public val minValue: Double? = null,
    public val maxValue: Double? = null,
    public val widgetHint: String? = null,
    public override val tags: Set<MemberTag> = emptySet(),
    public override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val readPermissions: Set<Permission> = permissions,
    override val writePermissions: Set<Permission> = permissions,
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    /**
     * A non-serializable, convenient representation of the value range.
     * Reconstructed from minValue and maxValue.
     */
    @Transient
    val valueRange: ClosedRange<Double>? = if (minValue != null && maxValue != null) {
        minValue..maxValue
    } else null

    /**
     * A list of allowed values for this property, derived from the [metaDescriptor].
     */
    val allowedValues: List<Value>? by metaDescriptor::allowedValues

    /**
     * A list of allowed value types for this property, derived from the [metaDescriptor].
     */
    val valueTypes: List<ValueType>? by metaDescriptor::valueTypes

    public companion object {
        public const val TYPE: String = "property"
    }
}
