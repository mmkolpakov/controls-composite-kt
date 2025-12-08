package space.kscience.controls.composite.model.contracts.logic

import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A factory responsible for creating a device instance.
 * The driver serves as the bridge between the abstract device model and the physical world (or a simulation).
 * It is a stateless, reusable component.
 *
 * The `DeviceDriver` contract is narrowly focused on a single responsibility:
 * 1.  **Instantiation**: The [create] method acts as a factory for the device object itself.
 *
 * All lifecycle logic, including resource acquisition (e.g., opening a socket) and release,
 * is explicitly defined in the [space.kscience.controls.composite.model.lifecycle.LifecyclePlans]
 * of a blueprint and orchestrated by a `TransactionCoordinator`. This ensures that the entire
 * lifecycle is declarative, transactional, and visible for introspection.
 *
 * @param D The type of the device contract this driver implements.
 */
public fun interface DeviceDriver<D : Device> {
    /**
     * Creates a new device instance from a **fully resolved** configuration.
     * This method is called by the runtime *after* all references (`${...}`) in the configuration meta
     * have been resolved (hydrated). The driver should not expect to handle dynamic references itself.
     * The provided meta is observable to support dynamic reconfiguration of driver-level parameters if needed.
     *
     * @param context The DataForge context for the device.
     * @param resolvedMeta The static, fully resolved configuration meta for the device.
     * @return A new instance of the device.
     */
    public fun create(context: Context, resolvedMeta: Meta): D
}
