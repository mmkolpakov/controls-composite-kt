package space.kscience.controls.composite.model.services

import space.kscience.controls.composite.model.contracts.logic.DeviceActionLogic
import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A service contract for a runtime plugin that can discover and provide instances of [DeviceActionLogic].
 * The runtime uses this service to resolve `logicId` references from an [ActionDescriptor] to a
 * concrete, executable implementation. This registry enables a library of reusable, versioned action logics.
 */
public interface ActionLogicRegistry : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Finds a [DeviceActionLogic] implementation by its unique hierarchical name and an optional version constraint.
     *
     * @param id The [Name] identifier of the logic.
     * @param version A version string or a version constraint (e.g., "1.2.0", "[1.0, 2.0)").
     *                If null, the provider should return the latest available version.
     * @return The found [DeviceActionLogic] instance, or `null` if no compatible logic is found.
     */
    public fun findById(id: Name, version: String?): DeviceActionLogic<*, *, *, *>?

    public companion object : PluginFactory<ActionLogicRegistry> {
        public const val ACTION_LOGIC_TARGET: String = "device.action.logic"
        override val tag: PluginTag = PluginTag("device.action.logic.registry", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime module.
         */
        override fun build(context: Context, meta: Meta): ActionLogicRegistry {
            error("ActionLogicRegistry is a service interface and requires a runtime-specific implementation.")
        }
    }
}
