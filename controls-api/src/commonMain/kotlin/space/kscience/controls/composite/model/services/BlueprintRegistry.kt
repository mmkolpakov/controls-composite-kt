package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that can discover and provide [DeviceBlueprintDeclaration] instances by their unique IDs.
 * This service is a critical component for building dynamic and distributed systems, as it allows a runtime
 * to discover the static contract of devices whose logic is not known at compile time.
 *
 * This registry is responsible **only** for the declarative part of the blueprint. The executable logic
 * is resolved separately via a `DeviceLogicRegistry`.
 *
 * In a distributed system, blueprints are expected to be available as pre-compiled artifacts on each node.
 * This registry serves as a service locator to find their declarative descriptions.
 */
public interface BlueprintRegistry : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Finds a [DeviceBlueprintDeclaration] by its unique identifier.
     *
     * @param id The type-safe [BlueprintId] of the blueprint.
     * @return The corresponding [DeviceBlueprintDeclaration], or `null` if no blueprint with that ID is found.
     */
    public fun findById(id: BlueprintId): DeviceBlueprintDeclaration?

    /**
     * Provides a collection of all known blueprint declarations in this registry.
     * This is useful for introspection and discovery by UIs or management tools.
     * @return A collection of all available [DeviceBlueprintDeclaration]s.
     */
    public fun getAll(): Collection<DeviceBlueprintDeclaration>

    public companion object : PluginFactory<BlueprintRegistry> {
        override val tag: PluginTag = PluginTag("device.blueprint.registry", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default implementation of this factory throws an error, as a concrete implementation
         * must be provided by a runtime or a dedicated service discovery module.
         */
        override fun build(context: Context, meta: Meta): BlueprintRegistry {
            error("BlueprintRegistry is a service interface and requires a runtime-specific implementation.")
        }
    }
}

/**
 * A convenience extension to get the [BlueprintRegistry] from a context.
 * Throws an error if the plugin is not installed, as it's considered essential for dynamic systems.
 */
public val Context.blueprintRegistry: BlueprintRegistry
    get() = plugins.find(true) { it is BlueprintRegistry } as? BlueprintRegistry
        ?: error("BlueprintRegistry plugin is not installed in the context.")
