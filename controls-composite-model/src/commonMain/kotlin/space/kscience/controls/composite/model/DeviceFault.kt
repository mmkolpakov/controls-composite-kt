package space.kscience.controls.composite.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A sealed interface representing a predictable, non-critical "business fault".
 * This is distinct from a system failure (represented by an exception). A fault is an expected
 * outcome of an operation under certain conditions, such as invalid input or a device being
 * in an incorrect state.
 *
 * Faults are serializable and can be transmitted as part of a regular (though negative)
 * response, allowing clients to handle them gracefully without treating them as unexpected errors.
 * All faults must be representable as [Meta] for interoperability.
 */
@Serializable
public sealed interface DeviceFault : MetaRepr {
    /**
     * A stable, machine-readable error code (e.g., "VALIDATION_ERROR").
     * This code is not intended for display to the user but for use in client-side logic
     * to reliably identify the type of fault. It should not change between minor versions.
     */
    public val code: String
}

/**
 * A fault indicating that the input provided for an operation failed validation.
 *
 * @property details A [Meta] object containing detailed information about the validation failure,
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
 * @param message A human-readable message explaining which precondition failed.
 * @property code A stable, machine-readable identifier for this fault type. Defaults to "PRECONDITION_FAILED".
 */
@Serializable
@SerialName("fault.precondition")
public data class PreconditionFault(
    val message: String,
    override val code: String = "PRECONDITION_FAILED",
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that a required resource (e.g., a physical port, a file lock)
 * is currently in use by another operation and cannot be accessed.
 */
@Serializable
@SerialName("fault.resourceBusy")
public data object ResourceBusyFault : DeviceFault {
    /**
     * A stable, machine-readable identifier for this fault type. Value is "RESOURCE_BUSY".
     */
    override val code: String = "RESOURCE_BUSY"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that an operation did not complete within its expected time frame.
 * This is distinct from a network or I/O timeout, representing a business-level timeout.
 */
@Serializable
@SerialName("fault.timeout")
public data object TimeoutFault : DeviceFault {
    /**
     * A stable, machine-readable identifier for this fault type. Value is "TIMEOUT".
     */
    override val code: String = "TIMEOUT"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}