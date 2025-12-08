package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.dataforge.names.Name

/**
 * A sealed interface representing the source of an action's executable logic.
 * This provides a strict, type-safe, and declarative way to specify how an action
 * should be executed, replacing the ambiguous use of multiple optional fields in [ActionDescriptor].
 *
 * Each implementation corresponds to a different strategy for defining or locating business logic.
 */
@Serializable
public sealed interface ActionLogicSource

/**
 * Specifies that the action's logic is implemented by an external, reusable, and versioned
 * component that implements [space.kscience.controls.composite.model.contracts.logic.DeviceActionLogic].
 * The runtime is responsible for resolving the [logicId] and [versionConstraint] via an `ActionLogicRegistry`.
 *
 * This is the preferred approach for complex, domain-specific, or frequently reused business logic.
 *
 * @property logicId The unique, hierarchical name of the external logic component.
 * @property versionConstraint An optional version string or range (e.g., "1.2.0", "[1.0, 2.0)") to ensure
 *                           that a compatible implementation is used.
 */
@Serializable
@SerialName("logic.external")
public data class ExternalLogic(
    val logicId: Name,
    val versionConstraint: String? = null
) : ActionLogicSource

/**
 * Specifies that the action's logic is defined inline as a declarative [TransactionPlan].
 * This allows for orchestrating a sequence of other actions, property writes, and control flow
 * operations directly within a blueprint.
 *
 * This is ideal for simple orchestration or for composing higher-level actions from more granular ones.
 *
 * @property plan The declarative [TransactionPlan] that constitutes this action's logic.
 */
@Serializable
@SerialName("logic.plan")
public data class PlanLogic(val plan: TransactionPlan) : ActionLogicSource

/**
 * Specifies that the action's logic is implemented by a `dataforge-data` [space.kscience.dataforge.workspace.Task].
 * This provides a bridge to the broader DataForge ecosystem, enabling complex data processing and
 * analysis workflows to be triggered as device actions.
 *
 * @property taskBlueprintId The unique identifier of the `TaskBlueprint` to be executed.
 */
@Serializable
@SerialName("logic.task")
public data class TaskLogic(val taskBlueprintId: String) : ActionLogicSource
