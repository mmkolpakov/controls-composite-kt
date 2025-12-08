package space.kscience.controls.composite.model.contracts.logic

import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A contract that encapsulates the executable business logic for a device's properties and actions.
 * This interface separates the "how" from the "what," allowing the declarative blueprint to define
 * members while this contract provides their implementation.
 *
 * Implementations of this interface are typically platform-specific and are provided at runtime.
 *
 * @param D The type of the device on which this logic operates.
 */
@InternalControlsApi
public interface InternalDeviceLogicFacade<in D : Device> {
    /**
     * The logic to read a property's value.
     * @param device The device instance.
     * @param propertyName The name of the property to read.
     * @return The property's value as a [Meta] object.
     */
    public suspend fun readProperty(device: D, propertyName: Name): Meta

    /**
     * The logic to write a value to a property.
     * @param device The device instance.
     * @param propertyName The name of the property to write.
     * @param value The new value.
     */
    public suspend fun writeProperty(device: D, propertyName: Name, value: Meta)

    /**
     * The logic to execute an action.
     * @param device The device instance.
     * @param actionName The name of the action to execute.
     * @param argument The optional argument for the action.
     * @return The optional result of the action.
     */
    public suspend fun executeAction(device: D, actionName: Name, argument: Meta?): Meta?
}
