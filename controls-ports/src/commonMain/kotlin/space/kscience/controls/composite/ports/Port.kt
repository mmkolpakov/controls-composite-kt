package space.kscience.controls.composite.ports

import kotlinx.coroutines.flow.Flow

/**
 * A raw byte port for low-level communication.
 *
 * A `Port` is a managed resource that must be explicitly opened before use and closed afterward.
 * The lifecycle of a port (opening, closing, and resource management) is the responsibility of its owner,
 * typically a [space.kscience.controls.composite.old.contracts.DeviceDriver], which should call
 * methods like `connect()` (for client ports) or `open()` (for server ports) within its `onStart` hook,
 * and `close()` within its `onStop` hook.
 *
 * @see SynchronousPort for request-response communication patterns.
 */
public interface Port : AutoCloseable {
    /**
     * Indicates whether the port is currently open and active.
     * Attempting to send data on a closed port will result in an exception.
     */
    public val isConnected: Boolean

    /**
     * Sends a raw byte array through the port. This is a suspendable function that may wait if an underlying
     * buffer is full, thus respecting backpressure.
     *
     * @param data The byte array to send.
     * @throws PortClosedException if the port is not open.
     * @throws PortTimeoutException if the send operation times out.
     */
    public suspend fun send(data: ByteArray)

    /**
     * A hot [Flow] of incoming byte arrays. The flow becomes active when the port is opened
     * and completes when it is closed. Observers should expect to handle raw chunks of data, which may
     * not correspond to complete messages.
     *
     * @return A hot [Flow] of byte arrays.
     */
    public fun receive(): Flow<ByteArray>
}

/**
 * An extension of [Port] for client-side ports that require an explicit connection step.
 */
public interface ConnectablePort : Port {
    /**
     * Establishes the underlying connection for this port.
     * This is a suspendable operation that completes when the connection is ready.
     * @throws PortException if the connection cannot be established.
     */
    public suspend fun connect()

    /**
     * Terminates the underlying connection and releases associated resources.
     */
    public suspend fun disconnect()
}

/**
 * A port that supports a synchronous, request-response communication pattern.
 */
public interface SynchronousPort : Port {
    /**
     * Sends a request and awaits a single, complete response.
     *
     * @param request The byte array representing the request.
     * @return The byte array representing the complete response.
     */
    public suspend fun respond(request: ByteArray): ByteArray
}

public open class PortException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
public class PortTimeoutException(message: String, cause: Throwable? = null) : PortException(message, cause)
public class PortClosedException(message: String, cause: Throwable? = null) : PortException(message, cause)