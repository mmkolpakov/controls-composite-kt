package space.kscience.controls.composite.model.specs.faults

import space.kscience.dataforge.names.Name
import kotlin.Result

/**
 * A sealed class hierarchy representing specific, high-level failures that can occur
 * during a DeviceHub operation. By inheriting from [Exception], these failures integrate
 * seamlessly with Kotlin's standard [Result] type.
 *
 * @param message A descriptive message for the exception.
 * @param cause The underlying throwable that caused this failure, if any.
 */
public sealed class HubFailure(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * A type alias for the result of a DeviceHub operation. It uses the standard [Result],
 * where the failure part is guaranteed to be an instance of [HubFailure].
 */
public typealias HubResult<T> = Result<T>

/**
 * A failure indicating that a requested device was not found in the hub.
 * @param name The name of the device that was not found.
 */
public data class DeviceNotFoundFailure(val name: Name) : HubFailure("Device with name '$name' not found in the hub.")

/**
 * A failure indicating that a high-level, multistep transaction (like attach, detach, or hotSwap) failed.
 * @param failure A detailed, serializable representation of the underlying error that caused the transaction to fail.
 */
public data class TransactionFailure(val failure: SerializableDeviceFailure) :
    HubFailure("Hub transaction failed: ${failure.message}")

/**
 * A failure indicating that a specific, single operation on a device (like readProperty or execute) failed.
 * @param failure A detailed, serializable representation of the underlying error.
 */
public data class OperationFailure(val failure: SerializableDeviceFailure) :
    HubFailure("Device operation failed: ${failure.message}")

/**
 * A generic failure for errors that do not fit into the other specific categories.
 * @param detailedMessage A human-readable message describing the failure.
 * @param underlyingFailure An optional, serializable representation of the underlying cause.
 */
public data class UnspecifiedHubFailure(
    val detailedMessage: String,
    val underlyingFailure: SerializableDeviceFailure?
) : HubFailure(detailedMessage)
