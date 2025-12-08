package space.kscience.controls.composite.model.contracts

import kotlinx.coroutines.flow.StateFlow
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.ExecutionContext
import space.kscience.controls.composite.model.contracts.communication.PeerConnectionResult
import space.kscience.controls.composite.model.contracts.communication.QoS
import space.kscience.dataforge.io.Envelope
import kotlin.time.Duration

/**
 * A contract for a runtime service that enables direct, efficient, peer-to-peer exchange of large binary data.
 * This mechanism bypasses the standard message bus to avoid overhead.
 */
public interface PeerConnection {
    /**
     * Companion object holding stable identifiers for the capability.
     */
    public companion object {
        /**
         * The unique, fully-qualified name for the [PeerConnection] capability.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.model.contracts.PeerConnection"
    }

    /**
     * A [StateFlow] indicating the current connection status of this peer link.
     * `true` if connected and ready to send/receive, `false` otherwise.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Establishes the underlying connection for this peer. This is a suspendable operation.
     * @return A [PeerConnectionResult] indicating success or a specific connection fault.
     */
    public suspend fun connect(): PeerConnectionResult<Unit>

    /**
     * Terminates the underlying connection and releases associated resources.
     */
    public suspend fun disconnect()

    /**
     * Retrieves an [Envelope] containing binary data from a peer device.
     *
     * @param address The network address of the target device.
     * @param contentId A unique identifier for the specific piece of content being requested.
     * @param context The [ExecutionContext] for this operation.
     * @param timeout An optional duration to wait for the operation to complete.
     * @return A [PeerConnectionResult] containing the requested [Envelope] on success (or null if not found),
     *         or a specific fault on failure.
     */
    public suspend fun receive(
        address: Address,
        contentId: String,
        context: ExecutionContext,
        timeout: Duration? = null,
    ): PeerConnectionResult<Envelope?>

    /**
     * Sends an [Envelope] containing binary data to a peer device.
     *
     * @param address The network address of the target device.
     * @param contentId A unique identifier for this piece of content.
     * @param envelope The envelope containing the binary data to send.
     * @param qos The desired Quality of Service for this transmission.
     * @param context The [ExecutionContext] for this operation.
     * @param timeout An optional duration to wait for the send operation to complete.
     * @return A [PeerConnectionResult] indicating success or a specific communication fault.
     */
    public suspend fun send(
        address: Address,
        contentId: String,
        envelope: Envelope,
        qos: QoS = QoS.AT_LEAST_ONCE,
        context: ExecutionContext,
        timeout: Duration? = null,
    ): PeerConnectionResult<Unit>
}
