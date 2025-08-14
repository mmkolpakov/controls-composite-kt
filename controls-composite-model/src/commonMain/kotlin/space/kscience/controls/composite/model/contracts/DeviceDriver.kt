package space.kscience.controls.composite.model.contracts

import space.kscience.controls.composite.model.meta.DeviceActionSpec
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.controls.composite.model.meta.MutableDevicePropertySpec
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A factory responsible for creating an instance of a device driver and managing its interactions.
 * The driver contains the actual hardware/simulation logic for a device. It serves as the bridge
 * between the abstract device model and the physical world.
 *
 * This is a `fun interface`, meaning its primary purpose is to create a device instance.
 * It also includes lifecycle hooks that the runtime will call at different stages,
 * allowing the driver to manage resources like connections or hardware initialization. These hooks
 * have default implementations.
 *
 * @param D The type of the device contract this driver implements.
 */
public fun interface DeviceDriver<D : Device> {
    /**
     * Creates a new device instance. This method should handle the initial setup of the device object itself.
     * @param context The DataForge context for the device.
     * @param meta The configuration meta for the device.
     * @return A new instance of the device.
     */
    public fun create(context: Context, meta: Meta): D

    /**
     * Reads the value of a property from the physical or virtual device.
     * This method is called by the runtime and should contain the specific logic to query the hardware or simulation.
     *
     * The implementation must ensure that the returned type matches the property's value type.
     *
     * @param device The device instance to read from.
     * @param spec The specification of the property being read.
     * @return The value of the property, or `null` if it cannot be read.
     */
    public suspend fun <T> read(device: D, spec: DevicePropertySpec<D, T>): T? =
        error("Read for property '${spec.name}' is not implemented in this driver.")

    /**
     * Writes a new value to a physical or virtual property.
     *
     * @param device The device instance to write to.
     * @param spec The specification of the property being written.
     * @param value The value to write. The type of the value is expected to match the spec.
     */
    public suspend fun <T> write(device: D, spec: MutableDevicePropertySpec<D, T>, value: T) {
        error("Write for property '${spec.name}' is not implemented in this driver.")
    }

    /**
     * Executes an action on the physical or virtual device.
     *
     * @param device The device instance to execute the action on.
     * @param spec The specification of the action being executed.
     * @param input The input argument for the action. The type is expected to match the spec.
     * @return The result of the action, or `null` if the action does not return a value.
     */
    public suspend fun <I, O> execute(device: D, spec: DeviceActionSpec<D, I, O>, input: I): O? =
        error("Execution for action '${spec.name}' is not implemented in this driver.")


    /**
     * A hook called by the runtime when the device is attached to the hub.
     * Use this for one-time setup that does not require the device to be active,
     * like validating configuration or preparing resources.
     * @param device The device instance being attached.
     */
    public suspend fun onAttach(device: D) {}

    /**
     * A hook called by the runtime when the device's start sequence is initiated.
     * This method should contain logic to prepare the device for operation,
     * such as opening connections or initializing hardware.
     * @param device The device instance being started.
     */
    public suspend fun onStart(device: D) {}

    /**
     * A hook called by the runtime immediately after the device has successfully started and
     * its lifecycle state has transitioned to [space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState.Running].
     * @param device The device instance that has started.
     */
    public suspend fun afterStart(device: D) {}

    /**
     * A hook called by the runtime when the device's stop sequence is initiated.
     * This method should contain logic for gracefully shutting down the device and releasing resources.
     * @param device The device instance being stopped.
     */
    public suspend fun onStop(device: D) {}

    /**
     * A hook called by the runtime after the device has successfully stopped.
     * @param device The device instance that has stopped.
     */
    public suspend fun afterStop(device: D) {}

    /**
     * A hook called when a reset is triggered on a failed device.
     * Use this to attempt to bring the device back to a stable, stopped state.
     * @param device The device instance being reset.
     */
    public suspend fun onReset(device: D) {}

    /**
     * A hook called when a device enters the [space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState.Failed] state.
     * @param device The device instance that failed.
     * @param error The optional error that caused the failure.
     */
    public suspend fun onFail(device: D, error: Throwable?) {}

    /**
     * A hook called by the runtime when the device is detached from the hub.
     * Use this for final cleanup. After this hook, the device instance should not be used.
     * @param device The device instance being detached.
     */
    public suspend fun onDetach(device: D) {}
}