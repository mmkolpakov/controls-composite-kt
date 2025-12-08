package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.specs.plans.TransactionPlan
import space.kscience.controls.composite.model.specs.state.HubActualStateDescriptor
import space.kscience.controls.composite.model.specs.state.HubStateDescriptor
import space.kscience.controls.composite.model.specs.state.StateDiff
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that calculates the difference between Desired and Actual states
 * and generates a convergence plan.
 *
 * This is the brain of the "Infrastructure as Code" (IaC) capability for the control system,
 * enabling Kubernetes-style reconciliation loops where the system autonomously moves towards
 * the target configuration defined in [HubStateDescriptor].
 */
public interface Reconciler : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Calculates the raw differences between the desired configuration and the actual runtime state.
     * This method is pure and does not change the state of the system.
     *
     * @param desired The target state description (Blueprint/GitOps).
     * @param actual The current state description (Runtime snapshot).
     * @return A list of specific differences ([StateDiff]).
     */
    public fun diff(
        desired: HubStateDescriptor,
        actual: HubActualStateDescriptor
    ): List<StateDiff>

    /**
     * Generates a transactional plan to resolve the provided differences.
     * The Runtime's [TransactionCoordinator] will be responsible for executing this plan.
     *
     * @param diffs The list of differences to resolve.
     * @return A [TransactionPlan] containing the necessary sequence of attach, detach, configure,
     *         and lifecycle actions to bring the system to the desired state.
     */
    public fun reconcile(
        diffs: List<StateDiff>
    ): TransactionPlan

    public companion object : PluginFactory<Reconciler> {
        override val tag: PluginTag = PluginTag("device.reconciler", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime.
         */
        override fun build(context: Context, meta: Meta): Reconciler {
            error("Reconciler is a service interface and requires a runtime-specific implementation.")
        }
    }
}
