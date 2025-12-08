package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.logic.BlueprintBehaviorFacet
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that discovers and provides [space.kscience.controls.composite.model.contracts.logic.BlueprintBehaviorFacet] instances by their unique IDs.
 * This service is the behavioral counterpart to the [BlueprintRegistry].
 *
 * The runtime uses this service to find the executable logic that corresponds to a given [space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration],
 * enabling the "hydration" of a blueprint into a fully executable [space.kscience.controls.composite.model.contracts.logic.ExecutableDeviceBlueprint].
 *
 * Implementations of this interface are expected to be provided by runtime modules, which can discover
 * logic providers via classpath scanning, service loaders, or other platform-specific mechanisms.
 */
public interface BlueprintBehaviorRegistry : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Finds a collection of all available logic provider facets for a given [space.kscience.controls.composite.model.contracts.BlueprintId].
     * The runtime is responsible for combining these facets into a complete logical picture.
     *
     * @param id The type-safe [space.kscience.controls.composite.model.contracts.BlueprintId] of the blueprint for which logic is required.
     * @return A collection of [space.kscience.controls.composite.model.contracts.logic.BlueprintBehaviorFacet]s. An empty collection if no providers are found.
     */
    public fun findFacets(id: BlueprintId): Collection<BlueprintBehaviorFacet<*>>

    /**
     * Provides a collection of all known logic facets in this registry.
     * This is useful for introspection and debugging.
     * @return A collection of all available [BlueprintBehaviorFacet]s.
     */
    public fun getAll(): Collection<BlueprintBehaviorFacet<*>>

    public companion object : PluginFactory<BlueprintBehaviorRegistry> {
        override val tag: PluginTag = PluginTag("device.logic.registry", group = PluginTag.Companion.DATAFORGE_GROUP)

        /**
         * The default implementation of this factory throws an error. A concrete implementation
         * must be provided by a runtime module.
         */
        override fun build(context: Context, meta: Meta): BlueprintBehaviorRegistry {
            error("DeviceLogicRegistry is a service interface and requires a runtime-specific implementation.")
        }
    }
}
