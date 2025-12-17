package space.kscience.controls.composite.old.contracts.runtime

import space.kscience.controls.core.InternalControlsApi
import space.kscience.controls.core.contracts.Device
import space.kscience.controls.composite.old.state.DeviceState

/**
 * An internal contract for a property delegate that can be "hydrated" by the runtime.
 * "Hydration" is the process of creating the actual, live [space.kscience.controls.composite.old.state.DeviceState] using the context
 * provided by a concrete device instance at runtime.
 *
 * This pattern decouples the DSL's declarative nature from the runtime's implementation details.
 * The DSL produces a `HydratableDeviceState`, and the `CompositeDevice` runtime is responsible for
 * calling [hydrate] to bring it to life.
 */
@InternalControlsApi
public fun interface HydratableDeviceState<in D : Device, out T> {
    /**
     * Creates the live [space.kscience.controls.composite.old.state.DeviceState] using the provided device instance as context.
     * This method is called by the runtime during device initialization.
     *
     * @param device The device instance that owns this state.
     * @return The fully initialized, reactive [space.kscience.controls.composite.old.state.DeviceState].
     */
    public fun hydrate(device: D): DeviceState<T>
}