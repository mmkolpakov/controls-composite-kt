package space.kscience.controls.composite.model.contracts.communication

import kotlinx.io.Buffer
import kotlin.time.Duration

/**
 * A contract for a synchronous, request-response style communication port.
 * This is essential for protocols like Modbus, SCPI, or simple serial command-response interactions
 * where the caller must wait for a specific reply before proceeding.
 *
 * Implementations of this interface are responsible for ensuring thread safety and
 * strictly matching responses to requests (e.g., via locking or correlation IDs).
 */
public interface SynchronousPort : AutoCloseable {

    /**
     * Sends a request and waits for a response.
     *
     * @param request The data payload to send.
     * @param timeout The maximum duration to wait for the response. If null, a default timeout
     *                defined by the port configuration will be used.
     * @return A [PeerConnectionResult] containing the response data buffer on success,
     *         or a specific fault (e.g. [space.kscience.controls.composite.model.specs.faults.Timeout]) on failure.
     */
    public suspend fun respond(
        request: Buffer,
        timeout: Duration? = null
    ): PeerConnectionResult<Buffer>

    /**
     * Executes a block of code that performs multiple operations on the port atomically.
     * This is used to ensure exclusive access to the port for a sequence of commands,
     * preventing interleaving with other operations from different threads.
     *
     * @param block A suspendable lambda that receives the locked [SynchronousPort].
     * @return The result of the block.
     */
    public suspend fun <R> transaction(block: suspend (SynchronousPort) -> R): R
}
