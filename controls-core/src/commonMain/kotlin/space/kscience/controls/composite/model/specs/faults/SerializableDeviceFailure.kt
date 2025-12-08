package space.kscience.controls.composite.model.specs.faults

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.DeviceFault
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.dataforge.meta.*

/**
 * A representation of a device failure, suitable for transmission over the network or between processes.
 * This structure captures the essential information about an exception without relying on platform-specific serialization.
 * It includes provisions for secure error reporting by making stack traces optional.
 */
@Serializable(with = SerializableDeviceFailure.Serializer::class)
public class SerializableDeviceFailure : Scheme() {
    /**
     * The simple class name of the original exception, used for identification.
     */
    public var type: String by string { "Unknown" }

    /**
     * The descriptive message of the failure.
     */
    public var message: String by string { "An unknown error occurred." }

    /**
     * An optional string representation of the stack trace.
     * **Security Note:** This should be null in production environments or when sending errors to untrusted clients
     * to avoid leaking internal implementation details.
     */
    public var stackTrace: String? by string()

    /**
     * Additional context-specific details about the error, provided as a [Meta] object.
     */
    public var details: Meta by value { Meta.EMPTY }

    /**
     * An optional, stable, machine-readable error code (e.g., "E-1024", "TIMEOUT").
     * This allows clients to programmatically handle specific error scenarios.
     */
    public var code: String? by string()

    /**
     * A flag indicating whether the operation that caused this failure can be safely retried.
     */
    public var retryable: Boolean by boolean(false)

    /**
     * If non-null, this indicates that the failure was a predictable business fault, not a system error.
     */
    public var fault: DeviceFault? by convertable(MetaConverter.serializable(PolymorphicSerializer(DeviceFault::class)))

    /**
     * An optional serializable representation of the underlying cause, allowing for nested error reporting.
     */
    public var cause: SerializableDeviceFailure? by schemeOrNull(SerializableDeviceFailure)

    public companion object : SchemeSpec<SerializableDeviceFailure>(::SerializableDeviceFailure)
    public object Serializer : SchemeAsMetaSerializer<SerializableDeviceFailure>(Companion)
}
