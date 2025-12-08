package space.kscience.controls.composite.model.contracts.hub

import space.kscience.controls.composite.model.specs.faults.SerializableDeviceFailure
import space.kscience.controls.composite.model.state.DataQuality

/**
 * Represents the outcome of a single operation within a batch request.
 * This allows for granular error handling where some items fail while others succeed,
 * which is critical for industrial dashboards and bulk data acquisition.
 */
public sealed interface OperationResult<out T> {
    /**
     * Represents a successful operation for a single item.
     * @property value The result value.
     * @property quality The quality of the returned value.
     */
    public data class Success<T>(val value: T, val quality: DataQuality) : OperationResult<T>

    /**
     * Represents a failure for a single item.
     * @property error The error details, serializable for network transmission.
     */
    public data class Failure(val error: SerializableDeviceFailure) : OperationResult<Nothing>
}
