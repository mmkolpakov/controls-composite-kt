package space.kscience.controls.composite.ports

import kotlinx.coroutines.flow.Flow
import space.kscience.controls.composite.model.lifecycle.ManagedComponent

/**
 * A raw byte port for low-level communication.
 * Its lifecycle is managed by the runtime via events. Implementations should handle resource allocation
 * (e.g., opening a socket) in their `onStart` hook and release them in `onStop`.
 */
public interface Port : ManagedComponent {
    /**
     * Sends a raw byte array through the port. This is a suspendable function that may wait if an underlying
     * buffer is full, thus respecting backpressure.
     *
     * @param data The byte array to send.
     * @throws PortClosedException if the port is not in a `Running` state.
     * @throws PortTimeoutException if the send operation times out.
     */
    public suspend fun send(data: ByteArray)

    /**
     * A hot [Flow] of incoming byte arrays.
     *
     * @return A hot [Flow] of byte arrays.
     */
    public fun receive(): Flow<ByteArray>
}

/**
 * A port that supports a synchronous, request-response communication pattern.
 * This is suitable for protocols where each command expects a single, well-defined reply.
 */
public interface SynchronousPort : Port {
    /**
     * Sends a request and awaits a single, complete response.
     *
     * @param request The byte array representing the request.
     * @return The byte array representing the complete response.
     * @throws PortTimeoutException if a response is not received within the configured timeout.
     * @throws PortClosedException if the port is not `Running`.
     */
    public suspend fun respond(request: ByteArray): ByteArray
}

// TODO
// public class PortException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
// public class PortTimeoutException(message: String, cause: Throwable? = null) : PortException(message, cause)
// public class PortClosedException(message: String, cause: Throwable? = null) : PortException(message, cause)