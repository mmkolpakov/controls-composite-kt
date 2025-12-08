package space.kscience.controls.composite.model.contracts.runtime

import kotlinx.coroutines.flow.StateFlow
import space.kscience.controls.composite.model.contracts.logic.PropertyHandler
import space.kscience.controls.composite.model.state.StateValue

/**
 * A context provided to the `logic` block of a device blueprint.
 * It serves as a type-safe accessor for the reactive [StateFlow] of device properties.
 */
public interface DeviceFlows {
    /**
     * Retrieves the [StateFlow] for a given property specification.
     * The runtime is responsible for creating and caching these flows.
     *
     * @param spec The [PropertyHandler] of the property.
     * @return A [StateFlow] that emits [StateValue] updates for the property.
     */
    public fun <T> flow(spec: PropertyHandler<*, T>): StateFlow<StateValue<T>>
}
