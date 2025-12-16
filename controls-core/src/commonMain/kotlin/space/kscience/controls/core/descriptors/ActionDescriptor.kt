package space.kscience.controls.core.descriptors

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import space.kscience.controls.core.identifiers.Permission
import space.kscience.controls.core.meta.AdapterBinding
import space.kscience.controls.core.meta.MemberTag
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.controls.core.spec.CachePolicy
import space.kscience.controls.core.spec.ResourceLockSpec
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * A serializable, self-contained descriptor for a device action. This object provides all the static information
 * about an action, which can be used for UI generation, validation, and remote invocation.
 *
 * @property name The unique, potentially hierarchical name of the action. Uses [Name] for consistency.
 * @property description An optional human-readable description of the action.
 * @property help An optional detailed help string, primarily used for documenting associated metrics.
 * @property group An optional string for grouping related actions in a user interface.
 * @property icon An optional string identifier for a visual icon, used by UIs.
 * @property defaultTimeout A recommended timeout for a single execution of this action's logic.
 * @property executionDeadline A hard deadline for the total duration of the action's execution.
 * @property requiredLocks A list of resource locks that must be acquired by the runtime before this action can be executed.
 * @property meta Additional arbitrary metadata for the action, like a serialized transaction plan.
 * @property inputMetaDescriptor A descriptor for the action's input [Meta].
 * @property outputDescriptor A descriptor for the action's output [Meta].
 * @property logicId If non-null, this action's implementation is provided by an external logic component.
 * @property logicVersionConstraint An optional version constraint for the external logic.
 * @property cachePolicy An optional policy defining how the results of this action should be cached.
 * @property taskBlueprintId If non-null, indicates that this action is implemented by a `dataforge-data` [Task].
 * @property distributable If `true`, a task-based action is a candidate for remote execution.
 * @property permissions The set of permissions required to execute this action.
 * @property metrics A [Meta] block for configuring metrics collection for this action.
 * @property labels A map of static labels to be attached to any metrics generated for this action.
 * @property operationalEventTypeName The FQN of an Event to post to the operational FSM on action execution.
 * @property operationalEventMeta Optional [Meta] to construct the operational event.
 * @property operationalSuccessEventTypeName The FQN of an Event to post on successful action execution.
 * @property operationalSuccessEventMeta Optional [Meta] to construct the success event.
 * @property operationalFailureEventTypeName The FQN of an Event to post on failed action execution.
 * @property operationalFailureEventMeta Optional [Meta] to construct the failure event.
 * @property taskInputTypeName String representation of the input KType for a Task-based action.
 * @property taskOutputTypeName String representation of the output KType for a Task-based action.
 * @property possibleFaults A set of FQNs of [DeviceFault]s that this action can predictably return.
 * @property requiredPredicates A set of names of predicate properties that must be `true` before this action can be executed.
 * @property tags A set of extensible, semantic [MemberTag]s for classification by external systems.
 * @property bindings A map of type-safe, protocol-specific configurations. The key is a unique string
 *                    identifying the protocol adapter, and the value is the serializable [AdapterBinding] configuration.
 */
@Serializable
public data class ActionDescriptor(
    public override val name: Name,
    public val description: String? = null,
    public val help: String? = null,
    public val group: String? = null,
    public val icon: String? = null,
    public val defaultTimeout: @Contextual Duration? = null,
    public val executionDeadline: @Contextual Duration? = null,
    public val requiredLocks: List<ResourceLockSpec> = emptyList(),
    public val meta: Meta = Meta.EMPTY,
    public val inputMetaDescriptor: MetaDescriptor = MetaDescriptor(),
    public val outputDescriptor: MetaDescriptor = MetaDescriptor(),
    public val logicId: Name? = null,
    public val logicVersionConstraint: String? = null,
    public val cachePolicy: CachePolicy? = null,
    public val taskBlueprintId: String? = null,
    public val distributable: Boolean = false,
    public val permissions: Set<Permission> = emptySet(),
    public val metrics: Meta = Meta.EMPTY,
    public val labels: Map<String, String> = emptyMap(),
    public val operationalEventTypeName: String? = null,
    public val operationalEventMeta: Meta? = null,
    public val operationalSuccessEventTypeName: String? = null,
    public val operationalSuccessEventMeta: Meta? = null,
    public val operationalFailureEventTypeName: String? = null,
    public val operationalFailureEventMeta: Meta? = null,
    public val taskInputTypeName: String? = null,
    public val taskOutputTypeName: String? = null,
    val possibleFaults: Set<String> = emptySet(),
    val requiredPredicates: Set<Name> = emptySet(),
    public override val tags: Set<MemberTag> = emptySet(),
    public override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val readPermissions: Set<Permission> = permissions,
    override val writePermissions: Set<Permission> = permissions,
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "action"
    }
}
