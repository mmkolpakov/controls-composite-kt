package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.plans.PlanExecutionContext
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.data.Data
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that resolves references and "hydrates" declarative configurations.
 * The implementation is responsible for both the low-level resolution of a single reference
 * and the high-level orchestration of hydrating an entire [Meta] object.
 */
public interface ReferenceResolver : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * The core business logic: resolves a single, structured [ComputableValue] object into a lazy [Data] object.
     * This method contains no parsing logic and operates on the canonical model.
     *
     * @param value The structured [ComputableValue] to resolve.
     * @param context The [PlanExecutionContext] providing the context for resolution (access to plan variables and environment).
     * @return A lazy [Data] object containing the resolved value, which could be a [Meta], [Value], or null.
     */
    public suspend fun resolve(value: ComputableValue, context: PlanExecutionContext): Data<Meta?>

    /**
     * A high-level orchestration function that "hydrates" a [Meta] object. It recursively traverses
     * the meta, finds all string templates, parses them into [ComputableValue]s, resolves them,
     * and constructs the final, fully-resolved [Meta] object.
     *
     * @param unresolvedMeta The meta object containing reference templates.
     * @param context The [PlanExecutionContext] for resolution.
     * @return A `Data<Meta>` representing the lazy hydration process. The result is the fully resolved Meta.
     */
    public fun hydrate(unresolvedMeta: Meta, context: PlanExecutionContext): Data<Meta>

    public companion object : PluginFactory<ReferenceResolver> {
        override val tag: PluginTag = PluginTag("device.reference.resolver", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime.
         */
        override fun build(context: Context, meta: Meta): ReferenceResolver {
            error("ReferenceResolver is a service interface and requires a runtime-specific implementation.")
        }
    }
}
