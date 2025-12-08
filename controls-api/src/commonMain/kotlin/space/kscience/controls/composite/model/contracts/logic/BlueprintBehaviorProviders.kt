package space.kscience.controls.composite.model.contracts.logic

import ru.nsk.kstatemachine.statemachine.BuildingStateMachine
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.runtime.DeviceFlows
import space.kscience.controls.composite.model.lifecycle.LifecycleContext
import space.kscience.controls.composite.model.meta.FsmDescriptor
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.dataforge.names.Name

/**
 * A marker interface for a provider of a specific piece of a device's executable logic.
 * This allows for a granular, compositional approach to defining a device's behavior.
 * Each facet is linked to a specific blueprint by its [blueprintId].
 *
 * @param D The type of the device this logic facet applies to.
 */
public interface BlueprintBehaviorFacet<in D : Device> {
    /**
     * The [space.kscience.controls.composite.model.contracts.BlueprintId] of the blueprint to which this logic facet belongs.
     * The runtime uses this ID to correctly associate logic with its declaration.
     */
    public val blueprintId: BlueprintId
}

/**
 * A base marker interface for a provider of a device's executable logic and lifecycle behavior.
 * This interface links a specific implementation of logic to a declarative blueprint via the [declaration].
 * Implementations are expected to be platform-specific and will reside in runtime modules.
 * This interface is **contravariant** on type `D` to allow a provider for a base device type
 * to be used for its subtypes.
 *
 * @param D The type of the device this provider manages.
 */
@InternalControlsApi
public interface BlueprintBehaviorProvider<in D : Device> {
    /**
     * The pure, serializable, platform-agnostic declaration of the device's structure and contract.
     * This property makes the link between the logic provider and its declaration explicit, serving as the
     * single source of truth for the device's static contract.
     */
    public val declaration: DeviceBlueprintDeclaration
}

/**
 * A provider for the behavioral contracts of a device's properties.
 * This is a logic facet that provides the implementation for properties.
 *
 * @param D The type of the device on which this logic operates.
 */
@InternalControlsApi
public interface PropertyHandlerProvider<in D : Device> : BlueprintBehaviorProvider<D>, BlueprintBehaviorFacet<D> {
    override val blueprintId: BlueprintId get() = declaration.id

    /**
     * A map of all property behavioral contracts provided by this logic.
     * The runtime will look up a contract in this map by property name and execute its logic.
     */
    public val propertyContracts: Map<Name, PropertyHandler<D, *>>
}

/**
 * A provider for the behavioral contracts of a device's actions.
 * This is a logic facet that provides the implementation for actions.
 *
 * @param D The type of the device on which this logic operates.
 */
@InternalControlsApi
public interface ActionHandlerProvider<in D : Device> : BlueprintBehaviorProvider<D>, BlueprintBehaviorFacet<D> {
    override val blueprintId: BlueprintId get() = declaration.id
    /**
     * A map of all action behavioral contracts provided by this logic.
     */
    public val actionContracts: Map<Name, ActionHandler<D, *, *>>
}

/**
 * A provider for the declarative Finite State Machine (FSM) definitions of a device.
 * This is a logic facet for lifecycle and operational FSMs.
 *
 * @param D The type of the device on which this logic operates.
 */
@InternalControlsApi
public interface DeviceFsmProvider<D : Device> : BlueprintBehaviorProvider<D>, BlueprintBehaviorFacet<D> {
    override val blueprintId: BlueprintId get() = declaration.id

    /**
     * A serializable descriptor of the lifecycle FSM's structure.
     * The runtime implementation is responsible for introspecting the state machine
     * defined in [lifecycle] to generate this descriptor. This is the single source of truth.
     */
    public val lifecycleDescriptor: FsmDescriptor

    /**
     * An optional serializable descriptor for the operational FSM's structure.
     * Null if no operational FSM is defined.
     */
    public val operationalFsmDescriptor: FsmDescriptor?


    /**
     * A suspendable DSL block that defines the device's lifecycle as a State Machine.
     */
    public val lifecycle: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit

    /**
     * An optional suspendable DSL block that defines the device's operational logic as a separate FSM.
     */
    public val operationalFsm: (suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit)?
}

/**
 * A provider for the internal reactive logic of a device.
 * This is a logic facet for setting up internal data flows and bindings.
 *
 * @param D The type of the device on which this logic operates.
 */
@InternalControlsApi
public interface DeviceReactiveLogicProvider<in D : Device> : BlueprintBehaviorProvider<D>, BlueprintBehaviorFacet<D> {
    override val blueprintId: BlueprintId get() = declaration.id
    /**
     * An optional block of code defining the internal reactive logic of the device,
     * executed by the runtime when the device starts. This is where property bindings,
     * derivations, and other reactive connections are established.
     */
    public val reactiveLogic: (suspend D.(DeviceFlows) -> Unit)?
}
