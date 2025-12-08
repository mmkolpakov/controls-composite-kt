package space.kscience.controls.composite.model.specs.faults

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * An interface representing predictable, non-critical faults that can occur during peer-to-peer communication.
 * This allows for structured error handling without relying on exceptions for control flow.
 * By implementing [MetaRepr], all faults can be consistently serialized for transport or logging.
 */
@Polymorphic
public interface PeerConnectionFault : MetaRepr {
    /**
     * A detailed, human-readable message describing the fault.
     */
    public val message: String

    /**
     * An optional underlying cause for the fault. This property is transient and will not be serialized.
     */
    @Transient
    public val cause: Throwable?
}

/**
 * A fault indicating that a connection could not be established or was lost.
 */
@Serializable
@SerialName("peer.fault.connectionFailed")
public data class ConnectionFailed(
    override val message: String = "Connection failed."
) : PeerConnectionFault {
    @Transient
    override val cause: Throwable? = null
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that an operation did not complete within the specified timeout.
 */
@Serializable
@SerialName("peer.fault.timeout")
public data class Timeout(
    override val message: String = "Operation timed out."
) : PeerConnectionFault {
    @Transient
    override val cause: Throwable? = null
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault indicating that the requested binary content was not found on the peer.
 */
@Serializable
@SerialName("peer.fault.contentNotFound")
public data class ContentNotFound(
    override val message: String = "Content not found."
) : PeerConnectionFault {
    @Transient
    override val cause: Throwable? = null
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A fault for other, unspecified communication errors.
 */
@Serializable
@SerialName("peer.fault.communicationError")
public data class CommunicationError(
    override val message: String = "An unspecified communication error occurred."
) : PeerConnectionFault {
    @Transient
    override val cause: Throwable? = null
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
