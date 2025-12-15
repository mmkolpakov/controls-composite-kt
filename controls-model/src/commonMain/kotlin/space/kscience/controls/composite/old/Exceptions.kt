package space.kscience.controls.composite.old

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A base, multiplatform-safe exception for security-related errors within the controls-composite framework.
 * This exception is thrown by services like [space.kscience.controls.composite.old.services.AuthorizationService]
 * when a [Principal] lacks the required [Permission].
 *
 * It inherits from [CompositeHubException] to ensure it is handled consistently with other framework-specific
 * operational errors and can be correctly serialized via [toSerializableFailure].
 */
public class DeviceSecurityException(message: String, cause: Throwable? = null) : CompositeHubException(message, cause)


/**
 * A serializable representation of a device failure, suitable for transmission over the network or between processes.
 * This structure captures the essential information about an exception without relying on platform-specific serialization.
 * It can represent both unexpected system failures and predictable business faults.
 *
 * @property type The simple class name of the original exception, used for identification.
 * @property message The descriptive message of the failure.
 * @property stackTrace An optional string representation of the stack trace, useful for remote debugging of system failures.
 * @property details Additional context-specific details about the error, provided as a [Meta] object.
 * @property code An optional, machine-readable error code (e.g., "E-1024", "TIMEOUT").
 * @property retryable A flag indicating whether the operation that caused this failure can be safely retried.
 * @property fault If non-null, this indicates that the failure was a predictable business fault, not a system error.
 *                 Clients can use the presence of this field to handle the outcome as a valid negative response
 *                 rather than an unexpected exception.
 * @property cause An optional serializable representation of the underlying cause, allowing for nested error reporting.
 */
@Serializable
public data class SerializableDeviceFailure(
    val type: String,
    val message: String,
    val stackTrace: String? = null,
    val details: Meta = Meta.EMPTY,
    val code: String? = null,
    val retryable: Boolean = false,
    val fault: DeviceFault? = null,
    val cause: SerializableDeviceFailure? = null,
)

/**
 * A base exception for all control operations on a [space.kscience.controls.composite.old.contracts.CompositeDeviceHub].
 * This provides a common type for callers to catch for any hub-related issues, ensuring consistent error handling.
 *
 * This class represents an unexpected system failure. For predictable business errors, use [DeviceFaultException].
 *
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception that caused this hub exception, if any.
 */
public open class CompositeHubException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    /**
     * Converts this exception into a serializable failure representation.
     * This method recursively converts the causal chain as long as the causes are also [CompositeHubException] instances.
     */
    public open fun toSerializableFailure(): SerializableDeviceFailure = SerializableDeviceFailure(
        type = this::class.simpleName ?: "CompositeHubException",
        message = message ?: "An unknown error occurred.",
        stackTrace = this.stackTraceToString(),
        cause = (cause as? CompositeHubException)?.toSerializableFailure()
    )
}

/**
 * A specialized exception used to signal a predictable business fault.
 * This exception should be thrown by device drivers or action logic to indicate an expected
 * negative outcome (e.g., validation failure, precondition not met), as opposed to an
 * unexpected system error.
 *
 * The runtime is expected to catch this exception and convert it into a `DeviceErrorMessage`
 * containing the [fault] details, treating it as a valid response rather than a system failure
 * that would trigger a rollback.
 *
 * @param fault The structured [DeviceFault] object describing the business error.
 * @param cause An optional underlying exception, though typically not needed for business faults.
 */
public class DeviceFaultException(public val fault: DeviceFault, cause: Throwable? = null) :
    CompositeHubException("A predictable business fault occurred: ${fault::class.simpleName}", cause) {
    /**
     * Overrides the base implementation to correctly populate the `fault` field in the
     * resulting [SerializableDeviceFailure].
     */
    override fun toSerializableFailure(): SerializableDeviceFailure =
        super.toSerializableFailure().copy(fault = this.fault)
}


/**
 * An exception indicating that a transaction containing one or more operations failed and was rolled back.
 * This exception typically wraps the specific underlying error that caused the transaction to fail.
 *
 * @param operation A string identifying the high-level operation that failed (e.g., "attach", "start").
 * @param targetDevice The name of the primary device involved in the transaction, if applicable.
 * @param cause The underlying exception that caused the transaction to fail.
 */
public class CompositeHubTransactionException(
    public val operation: String,
    public val targetDevice: Name? = null,
    cause: Throwable
) : CompositeHubException(
    "Transaction '$operation'${targetDevice?.let { " for device '$it'" } ?: ""} failed and was rolled back.",
    cause
) {
    /**
     * A constructor for cases where context is minimal.
     */
    public constructor(cause: Throwable) : this("Anonymous", null, cause)
}

/**
 * An exception indicating that an operation could not be completed because the target device was not found.
 *
 * @param name The name of the device that was not found.
 */
public class DeviceNotFoundInCompositeHubException(public val name: Name) :
    CompositeHubException("Device with name '$name' not found in the hub.")

/**
 * An exception indicating a failure during a device's lifecycle transition (e.g., startup or shutdown timeout).
 *
 * @param name The name of the device that failed the lifecycle operation.
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception, if any.
 */
public class DeviceLifecycleException(public val name: Name, message: String, cause: Throwable? = null) :
    CompositeHubException("Lifecycle operation for device '$name' failed: $message", cause)

/**
 * An exception for errors related to device property access.
 *
 * @param name The name of the device.
 * @param property The name of the property.
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception, if any.
 */
public class DevicePropertyException(
    public val name: Name,
    public val property: Name,
    message: String,
    cause: Throwable? = null
) : CompositeHubException("Property '$property' access on device '$name' failed: $message", cause)

/**
 * An exception for errors during the execution of a device action.
 *
 * @param name The name of the device.
 * @param action The name of the action.
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception, if any.
 */
public class DeviceActionException(
    public val name: Name,
    public val action: Name,
    message: String,
    cause: Throwable? = null
) : CompositeHubException("Action '$action' execution on device '$name' failed: $message", cause)