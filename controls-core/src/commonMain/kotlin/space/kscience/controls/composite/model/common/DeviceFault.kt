package space.kscience.controls.composite.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * An interface representing a predictable, non-critical "business fault".
 * This is distinct from a system failure (represented by an exception). A fault is an expected
 * outcome of an operation under certain conditions, such as invalid input or a device being
 * in an incorrect state.
 *
 * Faults are serializable and can be transmitted as part of a regular (though negative)
 * response, allowing clients to handle them gracefully without treating them as unexpected errors.
 * All faults must be representable as [Meta] for interoperability.
 */
public interface DeviceFault : MetaRepr {
    /**
     * A stable, machine-readable error code (e.g., "VALIDATION_ERROR").
     * This code is not intended for display to the user but for use in client-side logic
     * to reliably identify the type of fault. It should not change between minor versions.
     */
    public val code: String
}

/**
 * A generic fault implementation for errors that do not have a specialized schema but require
 * structured reporting.
 *
 * @property code The machine-readable error code.
 * @property message A human-readable description of the error.
 * @property details Additional context or debugging information in the form of [Meta].
 */
@Serializable
@SerialName("fault.generic")
public data class GenericDeviceFault(
    override val code: String,
    val message: String,
    val details: Meta = Meta.EMPTY
) : DeviceFault {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
