package space.kscience.controls.composite.model.contracts.runtime

import space.kscience.controls.composite.model.common.CompositeContext
import space.kscience.controls.composite.model.contracts.device.CompositeDevice
import space.kscience.controls.composite.model.contracts.logic.PropertyHandler
import space.kscience.controls.composite.model.contracts.logic.MutablePropertyHandler
import space.kscience.controls.composite.model.state.DeviceState
import space.kscience.controls.composite.model.state.MutableDeviceState
import kotlin.time.Clock

/**
 * A capability interface for any device that acts as a container for other devices
 * and manages a graph of reactive states. It extends [CompositeDevice], inheriting its
 * responsibilities as a container and a [Provider].
 *
 * This provides a clean, runtime-agnostic way for components like property delegates
 * to resolve children and states without depending on a specific implementation.
 * It is a key part of the dependency inversion between the DSL and Runtime modules.
 */
public interface DeviceContext : ReactiveScope, CompositeContext, CompositeDevice {

    /**
     * The clock associated with this device context, typically inherited from the host device.
     */
    override val clock: Clock

    /**
     * Gets a reactive [DeviceState] wrapper for a given read-only property specification.
     * The implementation is responsible for creating and caching this state.
     */
    public fun <T> getState(spec: PropertyHandler<*, T>): DeviceState<T>

    /**
     * Gets a reactive [MutableDeviceState] wrapper for a given mutable property specification.
     * The implementation is responsible for creating and caching this state, ensuring type safety.
     *
     * @param spec The full specification of the property, which carries type information.
     * @return The [MutableDeviceState] instance.
     */
    public fun <T> getMutableState(spec: MutablePropertyHandler<*, T>): MutableDeviceState<T>
}
