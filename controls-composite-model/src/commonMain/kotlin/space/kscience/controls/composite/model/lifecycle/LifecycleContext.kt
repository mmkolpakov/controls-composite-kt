package space.kscience.controls.composite.model.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ru.nsk.kstatemachine.event.Event
import ru.nsk.kstatemachine.state.IState
import ru.nsk.kstatemachine.statemachine.StateMachine
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.DeviceBlueprint
import space.kscience.controls.composite.model.contracts.DeviceDriver

/**
 * Provides a type-safe context for defining the lifecycle FSM within a [DeviceBlueprint].
 * It offers access to the device instance, its children, and the driver.
 *
 * This context serves as the bridge between the declarative FSM definition and the concrete device instance,
 * enabling reactive logic to be defined directly within lifecycle states.
 *
 * @param D The type of the device this context belongs to.
 */
public interface LifecycleContext<D : Device> {
    /**
     * The device instance on which the lifecycle FSM is operating.
     */
    public val device: D

    /**
     * The driver responsible for creating and managing the lifecycle hooks of the device.
     * The type uses an `in` projection because the driver is a "consumer" of the device type `D`
     * (it knows how to handle `D` or any of its supertypes).
     */
    public val driver: DeviceDriver<in D>

    /**
     * The dedicated [CoroutineScope] for this device instance.
     * All long-running operations and listeners related to the device's logic should be launched in this scope.
     */
    public val deviceScope: CoroutineScope

    /**
     * Retrieves an initialized child device by its blueprint.
     * This function is safe to call during FSM configuration to set up interactions between parent and child.
     */
    public suspend fun <CD : Device> child(blueprint: DeviceBlueprint<CD>): CD

    /**
     * Programmatically posts a new [Event] to the device's lifecycle FSM.
     * This allows for creating custom, logic-driven workflows within the state machine.
     *
     * @param event The lifecycle event to post to the FSM.
     */
    public suspend fun postEvent(event: Event)

    /**
     * Programmatically posts a new [Event] to the device's operational FSM, if it exists.
     * This is the primary mechanism for actions and internal logic to interact with the operational state.
     *
     * @param event The operational event to post.
     */
    public suspend fun postOperationalEvent(event: Event)

    /**
     * The operational Finite State Machine of the device, if it exists.
     * This provides a way to interact with the operational FSM from within the lifecycle FSM.
     */
    public val operationalFsm: StateMachine?

    /**
     * A [StateFlow] representing the current active state of the operational FSM.
     * Returns `null` if no operational FSM is defined for the device.
     * This allows lifecycle states to react to operational state changes.
     */
    public fun operationalFsmState(): StateFlow<IState>?
}