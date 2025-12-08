package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.logic.ExecutableDeviceBlueprint
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that discovers and provides fully executable blueprints.
 *
 * This service is the single source of truth for obtaining a device's complete blueprint,
 * which includes both its declarative structure and its executable logic. It replaces the separate
 * `BlueprintRegistry` and `DeviceLogicRegistry`, unifying the discovery process.
 *
 * The runtime uses this service to find and "hydrate" blueprints on demand, creating
 * [ExecutableDeviceBlueprint] instances that are ready for instantiation by a `CompositeDeviceHub`.
 */
public interface BlueprintProvider : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Finds and hydrates a [ExecutableDeviceBlueprint] by its unique identifier.
     *
     * @param id The type-safe [BlueprintId] of the blueprint.
     * @return The corresponding [ExecutableDeviceBlueprint], or `null` if no blueprint with that ID is found
     *         or if it cannot be fully hydrated (e.g., missing logic). The return type uses a star projection
     *         as the provider cannot know the specific device type `D` at lookup time.
     */
    public fun provide(id: BlueprintId): ExecutableDeviceBlueprint<*>?

    /**
     * Provides a collection of all known executable blueprints in this provider.
     * This is useful for introspection and discovery by UIs or management tools.
     * @return A collection of all available [ExecutableDeviceBlueprint]s.
     */
    public fun getAll(): Collection<ExecutableDeviceBlueprint<*>>


    public companion object : PluginFactory<BlueprintProvider> {
        override val tag: PluginTag = PluginTag("device.blueprint.provider", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime
         * or a dedicated service discovery module.
         */
        override fun build(context: Context, meta: Meta): BlueprintProvider {
            error("BlueprintProvider is a service interface and requires a runtime-specific implementation.")
        }
    }
}
