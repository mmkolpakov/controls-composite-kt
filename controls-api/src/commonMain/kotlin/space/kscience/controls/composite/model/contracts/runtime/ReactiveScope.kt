package space.kscience.controls.composite.model.contracts.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.device.StatefulDevice
import space.kscience.controls.composite.model.state.DeviceState
import space.kscience.controls.composite.model.state.MutableDeviceState
import space.kscience.controls.composite.model.state.okState
import space.kscience.controls.composite.model.state.value
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * An abstract element representing a piece of logic or state within a [ReactiveScope].
 * Used for introspection, visualization, and automated state management of the device's internal structure.
 */
public sealed interface ConstructorElement

/**
 * A [ConstructorElement] representing a single, independent state.
 */
public class StateConstructorElement<T>(public val state: DeviceState<T>) : ConstructorElement

/**
 * A [ConstructorElement] for an internal, logical state created via the `stateful` delegate.
 * It holds all necessary information for automatic state persistence and introspection, and encapsulates
 * the type-safe logic for snapshotting and restoring its own value.
 *
 * @property propertyName The name of the property backing this state. This name is used as the key
 *                        when serializing the state to a `Meta` object in a snapshot.
 * @property state The [MutableDeviceState] instance that holds the live value.
 * @property converter A [MetaConverter] for serializing the state's value to `Meta` and deserializing it back.
 *                     This is crucial for the automatic `snapshot` and `restore` mechanism.
 */
public class StatefulDelegateElement<T>(
    public val propertyName: String,
    public val state: MutableDeviceState<T>,
    public val converter: MetaConverter<T>,
) : ConstructorElement {
    /**
     * Creates a [Meta] representation of the current state value.
     * This method encapsulates the type-safe conversion, hiding the generic type `T`.
     * It is intended for internal use by the persistence mechanism.
     *
     * @return The state's value as a `Meta` object, or `null` if the current value is null.
     */
    @InternalControlsApi
    public fun snapshotValue(): Meta? {
        val currentValue = state.value
        return if (currentValue != null) converter.convert(currentValue) else null
    }

    /**
     * Restores the state from a [Meta] object.
     * This method encapsulates the type-safe deserialization and update, hiding the generic type `T`.
     * It is intended for internal use by the persistence mechanism.
     *
     * @param meta The `Meta` object to restore from.
     */
    @InternalControlsApi
    public suspend fun restoreValue(meta: Meta) {
        val restoredValue: T? = converter.readOrNull(meta)
        // Update the state with the restored value and mark it as OK.
        state.updateState(okState(restoredValue))
    }
}

/**
 * A [ConstructorElement] representing a connection or data flow between states.
 * @param reads The set of states that are inputs to this connection.
 * @param writes The set of states that are outputs of this connection.
 */
public class ConnectionConstructorElement(
    public val reads: Collection<DeviceState<*>>,
    public val writes: Collection<DeviceState<*>>,
) : ConstructorElement

/**
 * A contract for any component that can host and manage a graph of interconnected [DeviceState]s.
 * It provides a [CoroutineScope] for managing the lifecycle of reactive jobs and serves as a registry
 * for its internal [ConstructorElement]s, enabling advanced features like automatic state persistence
 * and introspection.
 */
public interface ReactiveScope : ContextAware, CoroutineScope {
    /**
     * A collection of all constructor elements within this container, allowing for introspection.
     */
    public val constructorElements: Set<ConstructorElement>

    /**
     * Registers a [ConstructorElement] with this container for tracking and management.
     * This method is typically called by property delegates or other DSL constructs during device initialization.
     */
    public fun registerElement(constructorElement: ConstructorElement)

    /**
     * Unregisters a previously registered [ConstructorElement].
     * This is typically called when a reactive job completes or a component is disposed of,
     * ensuring that the container does not hold stale references.
     */
    public fun unregisterElement(constructorElement: ConstructorElement)

    /**
     * A delegate factory for creating and automatically registering a **stateful, persistent property**.
     * This is the **only** correct way to define a property whose state should be managed by the
     * [StatefulDevice] persistence mechanism.
     *
     * This function **requires a [StatefulDevice] in its context**, making the dependency explicit at compile time.
     * Using this delegate ensures that the property is discoverable for automatic state saving and loading,
     * and that any changes to its value will correctly mark the device as "dirty".
     *
     * @param T The type of the property's value.
     * @param initialValue The initial value of the property.
     * @param converter A [MetaConverter] for serializing and deserializing the property's value.
     * @return A [PropertyDelegateProvider] that creates a read-write property delegate.
     */
    context(device: StatefulDevice)
    public fun <T> ReactiveScope.stateful(
        initialValue: T,
        converter: MetaConverter<T>,
    ): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> =
        PropertyDelegateProvider { thisRef, property ->
            // Ensure the delegate is being applied to the device instance provided in the context.
            require(thisRef === device) {
                "The `stateful` delegate must be used on the same StatefulDevice instance provided in the context."
            }

            // 1. Create the underlying mutable state holder.
            val state = VirtualMutableDeviceState(initialValue)

            // 2. Create the element that describes this stateful property for introspection and persistence.
            val element = StatefulDelegateElement(property.name, state, converter)
            // 3. Automatically register this element with the container.
            device.registerElement(element)

            // 4. Return the property delegate.
            object : ReadWriteProperty<Any?, T> {
                override fun getValue(thisRef: Any?, property: KProperty<*>): T =
                    state.value ?: initialValue // Fallback to initial value if state becomes null.

                override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                    // `device` (and its method markDirty) is available from the context parameter.
                    device.launch {
                        device.markDirty()
                    }
                    // Update the state's value.
                    device.launch {
                        state.update(value)
                    }
                }
            }
        }
}

/**
 * Subscribes to changes in a [DeviceState] and executes an action for each new value.
 * The action is not triggered for `null` values.
 *
 * @param T The type of the state's value.
 * @param container The [ReactiveScope] scope in which to launch the subscription job.
 * @param writes A collection of states that are modified by this action, for introspection.
 * @param reads A collection of states that are read by this action, for introspection.
 * @param onChange The suspendable action to perform on each new non-null value.
 * @return The [Job] managing this subscription. Cancelling the job will stop the subscription.
 */
public fun <T> DeviceState<T>.onNext(
    container: ReactiveScope,
    writes: Collection<DeviceState<*>> = emptySet(),
    reads: Collection<DeviceState<*>> = emptySet(),
    onChange: suspend (T) -> Unit,
): Job {
    val element = ConnectionConstructorElement(reads + this, writes)
    container.registerElement(element)
    return valueFlow.filterNotNull().onEach(onChange).launchIn(container).apply {
        invokeOnCompletion { container.unregisterElement(element) }
    }
}

/**
 * Subscribes to changes in a [DeviceState] and executes an action, providing both the previous and new values.
 * The action is only triggered if the new value is different from the previous one. Null values are included in comparisons.
 *
 * @param container The [ReactiveScope] scope in which to launch the subscription job.
 * @param onChange The suspendable action to perform, receiving the previous and new state values.
 * @return The [Job] managing this subscription.
 */
public fun <T> DeviceState<T>.onChange(
    container: ReactiveScope,
    writes: Collection<DeviceState<*>> = emptySet(),
    reads: Collection<DeviceState<*>> = emptySet(),
    onChange: suspend (prev: T?, next: T?) -> Unit,
): Job {
    val element = ConnectionConstructorElement(reads + this, writes)
    container.registerElement(element)
    return valueFlow.distinctUntilChanged().zip(valueFlow.drop(1)) { prev, next ->
        prev to next
    }.onEach { (prev, next) ->
        onChange(prev, next)
    }.launchIn(container).apply {
        invokeOnCompletion { container.unregisterElement(element) }
    }
}

/**
 * Binds the value of a [sourceState] to a [targetState]. Any update to the source will be
 * propagated to the target. This creates a one-way data flow.
 *
 * @return The [Job] managing the binding.
 */
public fun <T> ReactiveScope.bindState(
    sourceState: DeviceState<T>,
    targetState: MutableDeviceState<T>,
): Job {
    val element = ConnectionConstructorElement(setOf(sourceState), setOf(targetState))
    registerElement(element)

    return launch {
        // Initialize with the current state value
        targetState.updateState(sourceState.stateValue)
        // Propagate subsequent changes
        sourceState.stateFlow.collect {
            targetState.updateState(it)
        }
    }.apply {
        invokeOnCompletion { unregisterElement(element) }
    }
}
