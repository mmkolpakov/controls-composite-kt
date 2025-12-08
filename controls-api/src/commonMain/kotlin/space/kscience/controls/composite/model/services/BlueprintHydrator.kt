package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.logic.ExecutableDeviceBlueprint
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that "hydrates" a declarative blueprint into a fully executable one.
 * Hydration is the process of finding both the declarative part ([space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration])
 * and the behavioral part ([space.kscience.controls.composite.model.contracts.logic.BlueprintBehaviorProvider]) of a blueprint
 * and combining them into a single, executable [ExecutableDeviceBlueprint] instance.
 *
 * This service centralizes the logic for blueprint resolution and construction, decoupling the
 * [space.kscience.controls.composite.model.contracts.hub.DeviceHub] from the details of how blueprints
 * are discovered and assembled.
 */
public interface BlueprintHydrator : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Takes a blueprint ID, finds its declaration and logic from their respective registries,
     * and returns a fully executable blueprint.
     *
     * @param id The [BlueprintId] of the blueprint to hydrate.
     * @return The hydrated, executable blueprint.
     * @throws IllegalStateException if either the declaration or the logic provider for the given ID cannot be found.
     */
    public suspend fun hydrate(id: BlueprintId): ExecutableDeviceBlueprint<*>

    public companion object : PluginFactory<BlueprintHydrator> {
        override val tag: PluginTag = PluginTag("device.blueprint.hydrator", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error because BlueprintHydrator is a service contract
         * that requires a concrete, environment-specific implementation from a runtime module.
         */
        override fun build(context: Context, meta: Meta): BlueprintHydrator {
            error("BlueprintHydrator is a service interface and requires a runtime-specific implementation.")
        }
    }
}
