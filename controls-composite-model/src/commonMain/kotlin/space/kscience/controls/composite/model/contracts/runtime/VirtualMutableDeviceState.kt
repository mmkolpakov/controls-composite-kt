package space.kscience.controls.composite.model.contracts.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import space.kscience.controls.composite.model.state.MutableDeviceState
import space.kscience.controls.composite.model.state.Quality
import space.kscience.controls.composite.model.state.StateValue
import space.kscience.controls.composite.model.state.okState
import space.kscience.controls.composite.model.state.value

/**
 * A [MutableDeviceState] that does not correspond to a physical state,
 * holding its value in memory. It can be used for logical state management
 * within a composite device. The state is initialized with a non-null value.
 *
 * @param T The type of the value.
 * @param initialValue The non-null initial value of the state.
 */
public class VirtualMutableDeviceState<T>(initialValue: T) : MutableDeviceState<T> {

    private val flow = MutableStateFlow<StateValue<T?>>(okState(initialValue))

    override val stateFlow: StateFlow<StateValue<T?>> get() = flow
    override val stateValue: StateValue<T?> get() = flow.value

    override suspend fun update(value: T) {
        flow.value = okState(value)
    }

    override suspend fun updateState(stateValue: StateValue<T?>) {
        flow.value = stateValue
    }

    override suspend fun updateQuality(quality: Quality, message: String?) {
        // message is currently ignored in StateValue, but could be used for logging
        flow.value = flow.value.copy(quality = quality)
    }

    override fun toString(): String = "VirtualMutableDeviceState(value=$value)"
}