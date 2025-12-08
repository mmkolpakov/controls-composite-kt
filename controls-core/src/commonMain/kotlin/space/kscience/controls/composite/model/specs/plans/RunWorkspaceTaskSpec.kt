package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredMeta
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * An action that executes a `dataforge-data` task.
 */
@SerialName("runTask")
@Serializable(with = RunWorkspaceTaskSpec.Serializer::class)
public class RunWorkspaceTaskSpec : Scheme(), PlanActionSpec {
    /**
     * The unique ID of the `TaskBlueprint` to be executed.
     */
    public var taskBlueprintId: String by requiredString()

    /**
     * The [Meta] input for the task.
     */
    public var input: Meta by requiredMeta()

    /**
     * The key under which the task's result (`DataTree<*>`) will be stored in the `PlanExecutionContext`.
     */
    public var resultVariable: Name by requiredName()

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<RunWorkspaceTaskSpec>(::RunWorkspaceTaskSpec)
    public object Serializer : SchemeAsMetaSerializer<RunWorkspaceTaskSpec>(Companion)
}
