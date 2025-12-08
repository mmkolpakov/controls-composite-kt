package space.kscience.controls.composite.model.contracts.hub

import kotlinx.coroutines.flow.StateFlow
import space.kscience.dataforge.names.Name

/**
 * A contract for a proxy to a remote [DeviceHub].
 * This interface allows treating a remote hub as if it were local, abstracting away the
 * underlying communication mechanism. It extends the base hub contract with
 * connection management capabilities.
 */
public interface RemoteDeviceHub : DeviceHub {
    /**
     * The unique identifier of the remote hub this proxy connects to.
     */
    public val hubName: Name

    /**
     * A [StateFlow] indicating the current connection status to the remote hub.
     * `true` if connected and ready to send/receive, `false` otherwise.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Establishes the connection to the remote hub. This is a suspendable operation that
     * should be idempotent (calling it on an already connected hub should have no effect).
     *
     * @throws space.kscience.controls.composite.model.contracts.PeerConnectionException if the connection cannot be established.
     */
    public suspend fun connect()

    /**
     * Terminates the connection to the remote hub and releases associated resources.
     * This operation should be idempotent.
     */
    public suspend fun disconnect()
}
