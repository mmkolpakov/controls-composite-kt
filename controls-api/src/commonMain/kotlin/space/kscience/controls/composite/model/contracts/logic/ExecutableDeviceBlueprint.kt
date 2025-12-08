@file:OptIn(InternalControlsApi::class)

package space.kscience.controls.composite.model.contracts.logic

import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.specs.device.PropertyDescriptor
import space.kscience.controls.composite.model.specs.device.StreamDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name

/**
 * A complete, self-contained blueprint for a device, combining its declarative structure with its executable logic.
 *
 * This interface serves as a unified, "hydrated" view over a device's contract. It is constructed at runtime by a
 * `BlueprintHydrator`, which finds and combines the declarative [DeviceBlueprintDeclaration] with all its corresponding,
 * platform-specific logic facets.
 *
 * @param D The type of the device this blueprint creates.
 */
@DfType(ExecutableDeviceBlueprint.TYPE)
public interface ExecutableDeviceBlueprint<D : Device> : MetaRepr {
    /**
     * The pure, serializable, platform-agnostic declaration of the device's structure and contract.
     */
    public val declaration: DeviceBlueprintDeclaration

    /**
     * The non-serializable, platform-specific driver for creating the device instance. This component is
     * **mandatory** for a blueprint to be considered executable, as it is responsible for instantiation
     * and resource management.
     */
    @InternalControlsApi
    public val driver: DeviceDriver<D>

    /** The non-serializable provider for the device's property logic. Null if the device has no properties. */
    @InternalControlsApi
    public val propertyHandlerProvider: PropertyHandlerProvider<D>?

    /** The non-serializable provider for the device's action logic. Null if the device has no actions. */
    @InternalControlsApi
    public val actionHandlerProvider: ActionHandlerProvider<D>?

    /**
     * The non-serializable map of signal handlers.
     * Signals are often handled by the FSM provider, but explicit handlers can be registered here.
     */
    @InternalControlsApi
    public val signalHandlers: Map<Name, SignalHandler<D>>

    /** The non-serializable provider for the device's FSM definitions. Null if not hydrated. */
    @InternalControlsApi
    public val fsm: DeviceFsmProvider<D>?

    /** The non-serializable provider for the device's internal reactive logic. Null if not hydrated. */
    @InternalControlsApi
    public val reactiveLogic: DeviceReactiveLogicProvider<D>?


    // --- Convenience proxies to the declaration ---

    /** @see [DeviceBlueprintDeclaration.id] */
    public val id: BlueprintId get() = declaration.id

    /** @see [DeviceBlueprintDeclaration.version] */
    public val version: String get() = declaration.version

    /** @see [DeviceBlueprintDeclaration.properties] */
    public val properties: Collection<PropertyDescriptor> get() = declaration.properties

    /** @see [DeviceBlueprintDeclaration.actions] */
    public val actions: Collection<ActionDescriptor> get() = declaration.actions

    /** @see [DeviceBlueprintDeclaration.streams] */
    public val streams: Collection<StreamDescriptor> get() = declaration.streams

    /**
     * Serializes the declarative part of the blueprint into a [Meta] object.
     * The behavioral parts are not included.
     */
    override fun toMeta(): Meta = declaration.toMeta()

    public companion object {
        public const val TYPE: String = "device.blueprint"
    }
}

/**
 * A simple, concrete implementation of the [ExecutableDeviceBlueprint] interface.
 * This is typically used by a runtime or a `BlueprintHydrator` to create a fully "hydrated" blueprint instance
 * by combining a declaration with its corresponding logic facets.
 *
 * @param D The type of the device.
 * @property declaration The static, declarative part of the blueprint.
 * @property driver The driver for the device.
 * @property propertyHandlerProvider The provider for property read/write logic.
 * @property actionHandlerProvider The provider for action execution logic.
 * @property signalHandlers The map of executable signal handlers.
 * @property fsm The provider for FSM definitions.
 * @property reactiveLogic The provider for internal reactive logic.
 */
public data class CompositeExecutableDeviceBlueprint<D : Device>(
    override val declaration: DeviceBlueprintDeclaration,
    override val driver: DeviceDriver<D>,
    override val propertyHandlerProvider: PropertyHandlerProvider<D>?,
    override val actionHandlerProvider: ActionHandlerProvider<D>?,
    override val signalHandlers: Map<Name, SignalHandler<D>> = emptyMap(),
    override val fsm: DeviceFsmProvider<D>?,
    override val reactiveLogic: DeviceReactiveLogicProvider<D>?,
) : ExecutableDeviceBlueprint<D>
