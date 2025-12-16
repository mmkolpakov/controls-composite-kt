package space.kscience.controls.composite.old.contracts

import kotlinx.coroutines.flow.StateFlow
import space.kscience.controls.core.Address
import space.kscience.controls.composite.old.ExecutionContext
import space.kscience.dataforge.io.Envelope
import kotlin.time.Duration

/**
 * A base exception for errors related to peer-to-peer communication.
 */
public open class PeerConnectionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * An exception thrown when a peer-to-peer operation does not complete within the specified timeout.
 */
public class PeerConnectionTimeoutException(message: String, cause: Throwable? = null) : PeerConnectionException(message, cause)


/**
 * Quality of Service levels for peer-to-peer communication, ensuring reliability for critical operations.
 */
public enum class QoS {
    /**
     * "At-most-once": The message is sent, but delivery is not guaranteed. No acknowledgement is expected.
     * Suitable for high-frequency, non-critical data where some loss is acceptable.
     */
    AT_MOST_ONCE,

    /**
     * "At-least-once": Guarantees that the message will be delivered at least once, but duplicates are possible
     * in case of network issues and retries. The runtime must implement acknowledgement and retry mechanisms.
     * This is a good default for most state-changing operations.
     */
    AT_LEAST_ONCE,

    /**
     * "Exactly-once": Guarantees that the message will be delivered exactly once.
     * This is the most reliable but potentially the slowest level, requiring more complex coordination.
     */
    EXACTLY_ONCE
}


/**
 * A contract for a runtime service that enables direct, efficient, peer-to-peer exchange of large binary data,
 * represented as [Envelope]s. This mechanism bypasses the standard message bus to avoid overhead.
 *
 * This contract is defined in the old to allow blueprints to declare their capability
 * (via [space.kscience.controls.composite.old.features.BinaryDataFeature]) to use it. The actual implementation
 * is provided by the runtime.
 */
public interface PeerConnection {
    /**
     * Companion object holding stable identifiers for the capability.
     */
    public companion object {
        /**
         * The unique, fully-qualified name for the [PeerConnection] capability.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.old.contracts.PeerConnection"
    }

    /**
     * A [StateFlow] indicating the current connection status of this peer link.
     * `true` if connected and ready to send/receive, `false` otherwise.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Establishes the underlying connection for this peer.
     * This is a suspendable operation that completes when the connection is ready.
     * @throws PeerConnectionException if the connection cannot be established.
     */
    public suspend fun connect()

    /**
     * Terminates the underlying connection and releases associated resources.
     */
    public suspend fun disconnect()

    /**
     * Retrieves an [Envelope] containing binary data from a peer device.
     *
     * @param address The network address of the target device.
     * @param contentId A unique identifier for the specific piece of content being requested.
     * @param context The [ExecutionContext] providing security and tracing information for this operation.
     * @param timeout An optional duration to wait for the operation to complete. If null, a default timeout may be used.
     * @return The requested [Envelope], or `null` if the content is not found. The implementation may return a lazy-loaded envelope.
     * @throws PeerConnectionTimeoutException if the operation times out.
     * @throws PeerConnectionException for other communication errors.
     */
    public suspend fun receive(
        address: Address,
        contentId: String,
        context: ExecutionContext,
        timeout: Duration? = null,
    ): Envelope?

    /**
     * Sends an [Envelope] containing binary data to a peer device.
     *
     * @param address The network address of the target device.
     * @param contentId A unique identifier for this piece of content. This is crucial for the receiving end
     *                  to correctly identify and route the binary data.
     * @param envelope The envelope containing the binary data to send.
     * @param qos The desired Quality of Service for this transmission. The runtime will attempt to honor this level.
     *            The exact guarantees depend on the underlying transport implementation.
     * @param context The [ExecutionContext] for this operation.
     * @param timeout An optional duration to wait for the send operation to complete (e.g., to receive an acknowledgment
     *                for `AT_LEAST_ONCE` QoS). If null, a default timeout may be used.
     * @throws PeerConnectionTimeoutException if the operation times out.
     * @throws PeerConnectionException for other communication errors.
     */
    public suspend fun send(
        address: Address,
        contentId: String,
        envelope: Envelope,
        qos: QoS = QoS.AT_LEAST_ONCE,
        context: ExecutionContext,
        timeout: Duration? = null,
    )
}