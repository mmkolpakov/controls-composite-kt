package space.kscience.controls.composite.model.contracts.planning

import kotlinx.coroutines.flow.StateFlow
import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.composite.model.contracts.device.OperationalStateDescriptor

/**
 * A focused context providing safe, read-only access to a device's operational Finite State Machine (FSM).
 * This interface is passed to executable logic blocks (like [space.kscience.controls.composite.model.contracts.logic.DeviceActionLogic]) to allow them to interact
 * with the device's state in a controlled manner without exposing the entire device instance or its lifecycle controls.
 *
 * This abstraction ensures that action logic can be state-aware and can drive state transitions,
 * but cannot, for example, directly stop or reconfigure the device, adhering to the principle of least privilege.
 */
public interface FsmContext {
    /**
     * The current active state of the operational FSM, represented by a serializable descriptor.
     * Provides a reactive way to observe the device's internal state across different platforms and network boundaries.
     */
    public val state: StateFlow<OperationalStateDescriptor?>

    /**
     * Asynchronously posts an [ru.nsk.kstatemachine.event.Event] to the operational FSM, triggering a potential state transition.
     * This is the primary mechanism for action logic to influence the device's operational state.
     * The method is "fire-and-forget" from the caller's perspective.
     *
     * @param event The event to post to the FSM.
     */
    public suspend fun post(event: Event)
}
