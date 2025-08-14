package space.kscience.controls.composite.model.contracts

import kotlinx.coroutines.flow.StateFlow

/**
 * A contract for a proxy to a remote [CompositeDeviceHub].
 * This interface allows treating a remote hub as if it were local, abstracting away the
 * underlying communication mechanism. It extends the base hub contract with
 * connection management capabilities.
 */
public interface RemoteCompositeDeviceHub : CompositeDeviceHub {
    /**
     * The unique identifier of the remote hub this proxy connects to.
     */
    public val hubId: String

    /**
     * A [StateFlow] indicating the current connection status to the remote hub.
     */
    public val isConnected: StateFlow<Boolean>

    /**
     * Establishes the connection to the remote hub. This is a suspendable operation.
     * Throws an exception if the connection cannot be established.
     */
    public suspend fun connect()

    /**
     * Terminates the connection to the remote hub.
     */
    public suspend fun disconnect()
}