package space.kscience.controls.composite.model.contracts.logic

import ru.nsk.kstatemachine.statemachine.StateMachineDslMarker
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.controls.composite.model.specs.device.PropertyDescriptor
import space.kscience.controls.composite.model.specs.device.SignalDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.names.Name
import kotlin.reflect.KType

/**
 * A behavioral contract for a device's read-only property.
 * This interface defines the executable logic for a property and is part of the
 * **implementation** of a device's logic, not its public-facing model.
 * It is linked to a declarative [space.kscience.controls.composite.model.specs.device.PropertyDescriptor] at runtime.
 *
 * @param D The type of the device this property logic belongs to.
 * @param T The type of the property's value.
 */
@StateMachineDslMarker
public interface PropertyHandler<in D : Device, T> {
    /**
     * The unique, hierarchical name of the property. This must match the name in the corresponding [PropertyDescriptor].
     */
    public val name: Name get() = descriptor.name

    /**
     * The [MetaConverter] for the property's value type [T], used for serialization and deserialization.
     */
    public val converter: MetaConverter<T>

    /**
     * The [KType] of the property's value. Essential for runtime type validation in bindings.
     */
    public val valueType: KType

    /**
     * The suspendable logic to read the property's value from the device instance.
     * @param device The instance of the device on which to perform the read operation.
     * @return The current value of the property, or null if it cannot be read.
     */
    public suspend fun read(device: D): T?

    public val descriptor: PropertyDescriptor
}

/**
 * A behavioral contract for a mutable device property, extending the read-only version with write logic.
 */
@StateMachineDslMarker
public interface MutablePropertyHandler<in D : Device, T> : PropertyHandler<D, T> {
    /**
     * The suspendable logic to write a new value to the property on the device instance.
     * @param device The instance of the device.
     * @param value The new value to be written.
     */
    public suspend fun write(device: D, value: T)
}

/**
 * A behavioral contract for a device's action.
 *
 * @param D The type of the device this action belongs to.
 * @param I The type of the action's input.
 * @param O The type of the action's output.
 */
@StateMachineDslMarker
public interface ActionHandler<in D : Device, I, O> {
    /**
     * The unique, hierarchical name of the action. This must match the name in the corresponding [ActionDescriptor].
     */
    public val name: Name get() = descriptor.name

    /**
     * The [MetaConverter] for the action's input type [I].
     */
    public val inputConverter: MetaConverter<I>

    /**
     * The [MetaConverter] for the action's output type [O].
     */
    public val outputConverter: MetaConverter<O>

    /**
     * The suspendable logic to execute the action on the device instance.
     * @param device The instance of the device.
     * @param input The input argument for the action.
     * @return The result of the action, or `null` if the action does not produce a result.
     */
    public suspend fun execute(device: D, input: I): O?

    public val descriptor: ActionDescriptor
}

/**
 * A behavioral contract for a device's signal.
 * A signal is a lightweight command that typically triggers a state transition via the operational FSM.
 *
 * @param D The type of the device this signal belongs to.
 */
@StateMachineDslMarker
public interface SignalHandler<in D : Device> {
    /**
     * The unique name of the signal. Matches [SignalDescriptor.name].
     */
    public val name: Name get() = descriptor.name

    /**
     * The declarative descriptor for this signal.
     */
    public val descriptor: SignalDescriptor

    /**
     * Processes the signal logic.
     * Typically, this involves resolving the event type from the descriptor and posting it to the device's FSM.
     *
     * @param device The device instance.
     * @param argument Optional metadata payload accompanying the signal.
     */
    public suspend fun process(device: D, argument: Meta?)
}
