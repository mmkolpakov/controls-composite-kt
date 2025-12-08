package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.alarms.AlarmSeverity
import space.kscience.controls.composite.model.common.Permission
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * A serializable, declarative specification for a single alarm condition within a device blueprint.
 * This makes alarms first-class members of a device, just like properties and actions, allowing for
 * static analysis, validation, and introspection.
 *
 * @property name The unique, hierarchical name of the alarm (e.g., "temperature.high").
 * @property description A human-readable description of what the alarm signifies.
 * @property predicateName The name of a boolean property (a `PREDICATE`) on the same device that triggers this alarm.
 *                         When this property's value is `true`, the alarm condition is considered active.
 * @property severity The importance level of this alarm.
 * @property retainTime An optional duration for which the alarm should remain in an active state even after the
 *                      triggering predicate becomes false. This is useful for capturing transient, short-lived alarm conditions
 *                      that might otherwise be missed by operators.
 * @property policies A unified specification for all operational policies, such as display hints and metrics.
 * @property readPermissions Permissions required to read.
 * @property writePermissions Permissions required to write.
 * @property tags A set of extensible, semantic tags for classification.
 * @property bindings A map of type-safe, protocol-specific configurations, for example, to map this alarm
 *                    to a specific tag in an external SCADA system.
 */
@Serializable
public data class AlarmDescriptor(
    override val name: Name,
    val description: String,
    val predicateName: Name,
    val severity: AlarmSeverity,
    val retainTime: Duration = Duration.ZERO,
    override val policies: MemberPolicies = MemberPolicies(),
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
