package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.plans.PlanExecutionContext
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that resolves [ComputableValue] instances within the context of a [TransactionPlan].
 * This service is a key component of the plan execution engine, responsible for "hydrating" declarative
 * references into concrete values before an action is executed.
 */
public interface PlanReferenceResolver : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Resolves a [ComputableValue] into a concrete [Meta] object.
     *
     * @param computableValue The declarative value to resolve.
     * @param context The [PlanExecutionContext], which provides access to intermediate results and environment data.
     * @return The resolved [Meta] object, or `null` if the reference points to a null value.
     * @throws RuntimeException if the reference cannot be resolved (e.g., points to a non-existent value).
     */
    public suspend fun resolve(computableValue: ComputableValue, context: PlanExecutionContext): Meta?

    public companion object : PluginFactory<PlanReferenceResolver> {
        override val tag: PluginTag = PluginTag("device.plan.reference.resolver", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime.
         */
        override fun build(context: Context, meta: Meta): PlanReferenceResolver {
            error("PlanReferenceResolver is a service interface and requires a runtime-specific implementation.")
        }
    }
}
