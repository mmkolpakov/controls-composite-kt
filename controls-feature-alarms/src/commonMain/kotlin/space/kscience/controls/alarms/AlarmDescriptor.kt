package space.kscience.controls.alarms

import kotlinx.serialization.Serializable
import space.kscience.controls.core.descriptors.MemberDescriptor
import space.kscience.controls.core.identifiers.Permission
import space.kscience.controls.core.meta.AdapterBinding
import space.kscience.controls.core.meta.MemberTag
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * A serializable, declarative specification for a single alarm condition within a device blueprint.
 *
 * @property predicateName The name of a boolean property (a `PREDICATE`) on the same device that triggers this alarm.
 * @property retainTime An optional duration for which the alarm should remain in an active state even after the
 *                      triggering predicate becomes false.
 */
@Serializable
public data class AlarmDescriptor(
    override val name: Name,
    val description: String,
    val predicateName: Name,
    val severity: AlarmSeverity,
    val retainTime: Duration = Duration.ZERO,
    // Policies to be added later if needed via Meta or extensions
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}