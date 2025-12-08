package space.kscience.controls.composite.model.specs.faults

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.composite.model.common.DeviceFault
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A fault indicating that the input provided for an operation failed validation.
 *
 * @property details A [space.kscience.dataforge.meta.Meta] object containing detailed information about the validation failure,
 *                   such as which fields were invalid and why.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "VALIDATION_ERROR".
 */
@Serializable
@SerialName("fault.validation")
public data class ValidationFault(
    val details: Meta,
    override val code: String = "VALIDATION_ERROR",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that a required precondition for an operation was not met.
 *
 * @property message A human-readable message explaining which precondition failed.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "PRECONDITION_FAILED".
 */
@Serializable
@SerialName("fault.precondition")
public data class PreconditionFault(
    val message: String = "A precondition for the operation was not met.",
    override val code: String = "PRECONDITION_FAILED",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that a required resource (e.g., a physical port, a file lock)
 * is currently in use by another operation and cannot be accessed.
 *
 * @property code A stable, machine-readable identifier for this fault type. Value is "RESOURCE_BUSY".
 */
@Serializable
@SerialName("fault.resourceBusy")
public data class ResourceBusyFault(
    override val code: String = "RESOURCE_BUSY",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that an operation did not complete within its expected time frame.
 * This is distinct from a network or I/O timeout, representing a business-level timeout.
 *
 * @property code A stable, machine-readable identifier for this fault type. Value is "TIMEOUT".
 */
@Serializable
@SerialName("fault.timeout")
public data class TimeoutFault(
    override val code: String = "TIMEOUT",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A standard fault indicating that a requested resource (e.g., a device, property, or action) could not be found.
 *
 * @property resourceType A string describing the type of resource that was not found (e.g., "Device", "Property").
 * @property resourceId A string identifier for the missing resource.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "NOT_FOUND".
 */
@Serializable
@SerialName("fault.notFound")
public data class NotFoundFault(
    val resourceType: String = "Unknown",
    val resourceId: String = "Unknown",
    override val code: String = "NOT_FOUND",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A standard fault indicating that an authentication attempt failed.
 *
 * @property reason A human-readable message explaining why authentication failed (e.g., "Invalid credentials").
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "AUTHENTICATION_FAILED".
 */
@Serializable
@SerialName("fault.authentication")
public data class AuthenticationFault(
    val reason: String = "Authentication failed.",
    override val code: String = "AUTHENTICATION_FAILED",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A standard fault indicating that an authenticated principal is not authorized to perform an operation.
 *
 * @property principalName The name of the principal who was denied access.
 * @property requiredPermission The permission that was required for the operation.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "AUTHORIZATION_DENIED".
 */
@Serializable
@SerialName("fault.authorization")
public data class AuthorizationFault(
    val principalName: String = "Unknown",
    val requiredPermission: String = "Unknown",
    override val code: String = "AUTHORIZATION_DENIED",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A standard fault indicating that an operation was attempted while the device was in an incompatible state.
 *
 * @property currentState The name of the state the device was in.
 * @property requiredState A description of the state(s) required to perform the operation.
 * @property operation The name of the operation that was attempted.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "INVALID_STATE".
 */
@Serializable
@SerialName("fault.invalidState")
public data class InvalidStateFault(
    val currentState: String = "Unknown",
    val requiredState: String = "Unknown",
    val operation: String = "Unknown",
    override val code: String = "INVALID_STATE",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A base, multiplatform-safe exception for security-related errors within the controls-composite framework.
 * This exception is thrown by services like [space.kscience.controls.composite.model.services.AuthorizationService]
 * when a [space.kscience.controls.composite.model.common.Principal] lacks the required [space.kscience.controls.composite.model.common.Permission].
 *
 * It inherits from [HubException] to ensure it is handled consistently with other framework-specific
 * operational errors and can be correctly serialized via [toSerializableFailure].
 */
public class DeviceSecurityException(message: String, cause: Throwable? = null) : HubException(message, cause)

/**
 * A base exception for all control operations on a [space.kscience.controls.composite.model.contracts.hub.DeviceHub].
 * This provides a common type for callers to catch for any hub-related issues, ensuring consistent error handling.
 *
 * This class represents an unexpected system failure. For predictable business errors, use [DeviceFaultException].
 *
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception that caused this hub exception, if any.
 */
public open class HubException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    /**
     * Converts this exception into a serializable failure representation.
     * This method recursively converts the causal chain as long as the causes are also [HubException] instances.
     */
    public open fun toSerializableFailure(): SerializableDeviceFailure = SerializableDeviceFailure {
        type = this@HubException::class.simpleName ?: "CompositeHubException"
        message = this@HubException.message ?: "An unknown error occurred."
        stackTrace = this@HubException.stackTraceToString()
        cause = (this@HubException.cause as? HubException)?.toSerializableFailure()
    }
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
    HubException("A predictable business fault occurred: ${fault::class.simpleName}", cause) {
    /**
     * Overrides the base implementation to correctly populate the `fault` field in the
     * resulting [SerializableDeviceFailure].
     */
    override fun toSerializableFailure(): SerializableDeviceFailure =
        super.toSerializableFailure().apply {
            this.fault = this@DeviceFaultException.fault
        }
}

/**
 * An exception indicating that a transaction containing one or more operations failed and was rolled back.
 * This exception typically wraps the specific underlying error that caused the transaction to fail.
 *
 * @param operation A string identifying the high-level operation that failed (e.g., "attach", "start").
 * @param targetDevice The name of the primary device involved in the transaction, if applicable.
 * @param cause The underlying exception that caused the transaction to fail.
 */
public class HubTransactionException(
    public val operation: String,
    public val targetDevice: Name? = null,
    cause: Throwable
) : HubException(
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
public class DeviceNotFoundInHubException(public val name: Name) :
    HubException("Device with name '$name' not found in the hub.")

/**
 * An exception indicating a failure during a device's lifecycle transition (e.g., startup or shutdown timeout).
 *
 * @param name The name of the device that failed the lifecycle operation.
 * @param message A descriptive message of the failure.
 * @param cause The underlying exception, if any.
 */
public class DeviceLifecycleException(public val name: Name, message: String, cause: Throwable? = null) :
    HubException("Lifecycle operation for device '$name' failed: $message", cause)

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
) : HubException("Property '$property' access on device '$name' failed: $message", cause)

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
) : HubException("Action '$action' execution on device '$name' failed: $message", cause)
