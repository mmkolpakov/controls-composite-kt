package space.kscience.controls.composite.model.contracts.runtime

import kotlinx.coroutines.channels.Channel
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.dataforge.meta.Meta

/**
 * An internal data class defining the structure for property update messages.
 * This is used by the stateful property mechanism to communicate changes without
 * exposing implementation details.
 */
public data class StateUpdate(public val propertyName: String, public val meta: Meta)

/**
 * A capability interface defining the contract for a service that provides
 * instance-specific update channels for devices. This abstraction is key to
 * the Dependency Inversion Principle, allowing the DSL to depend on this
 * contract rather than a concrete runtime implementation.
 */
public interface StatefulPropertyUpdateChannelProvider {
    /**
     * Retrieves or creates a dedicated update channel for the given device instance.
     *
     * @param device The device for which the channel is requested.
     * @return A [Channel] for posting [StateUpdate] messages.
     */
    public suspend fun getPropertyUpdateChannel(device: Device): Channel<StateUpdate>
}
