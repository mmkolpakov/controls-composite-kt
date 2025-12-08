package space.kscience.controls.composite.model.contracts.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import space.kscience.controls.composite.model.state.DataQuality
import space.kscience.controls.composite.model.state.MutableDeviceState
import space.kscience.controls.composite.model.state.StateValue
import space.kscience.controls.composite.model.state.okState
import space.kscience.controls.composite.model.state.value
import kotlin.time.Clock

/**
 * A [MutableDeviceState] that does not correspond to a physical state,
 * holding its value in memory. It can be used for logical state management
 * within a composite device. The state is initialized with a non-null value.
 *
 * @param T The type of the value.
 * @param initialValue The non-null initial value of the state.
 */
public class VirtualMutableDeviceState<T>(
    initialValue: T,
    private val clock: Clock = Clock.System
) : MutableDeviceState<T> {

    private val _flow = MutableStateFlow<StateValue<T?>>(okState(initialValue, clock))

    private val mutex = Mutex()

    override val stateFlow: StateFlow<StateValue<T?>> = _flow.asStateFlow()
    override val stateValue: StateValue<T?> get() = _flow.value

    override suspend fun update(value: T) {
        mutex.withLock {
            _flow.value = okState(value, clock)
        }
    }

    override suspend fun updateState(stateValue: StateValue<T?>) {
        mutex.withLock {
            _flow.value = stateValue
        }
    }

    override suspend fun updateQuality(quality: DataQuality) {
        mutex.withLock {
            val current = _flow.value
            if (current.quality != quality) {
                _flow.value = current.copy(quality = quality)
            }
        }
    }

    override fun toString(): String = "VirtualMutableDeviceState(value=$value)"
}