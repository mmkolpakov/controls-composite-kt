package space.kscience.controls.composite.ktor.ports

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.io.readByteArray
import space.kscience.controls.composite.ports.ConnectablePort
import space.kscience.controls.composite.ports.PortClosedException
import space.kscience.controls.composite.ports.PortException
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.dataforge.meta.*

/**
 * A [Port] implementation for UDP sockets based on Ktor.
 * The port's lifecycle is managed externally; it must be connected before use and closed afterward.
 */
public class KtorUdpPort private constructor(
    public val context: Context,
    private val remoteHost: String,
    private val remotePort: Int,
    private val localHost: String?,
    private val localPort: Int?,
    private val meta: Meta,
    private val socketOptions: SocketOptions.UDPSocketOptions.() -> Unit,
) : ConnectablePort {

    private val scope = CoroutineScope(context.coroutineContext + SupervisorJob(context.coroutineContext[Job]))
    private var socket: BoundDatagramSocket? = null
    private var readJob: Job? = null
    private val incoming = MutableSharedFlow<ByteArray>()

    override val isConnected: Boolean get() = socket?.isClosed == false && readJob?.isActive == true

    override suspend fun connect() {
        if (isConnected) {
            context.logger.warn { "Port $this is already connected." }
            return
        }
        try {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val localAddress = localPort?.let { InetSocketAddress(localHost ?: "0.0.0.0", it) }
            val clientSocket = aSocket(selectorManager).udp().bind(localAddress, socketOptions)
            this.socket = clientSocket

            readJob = scope.launch(currentCoroutineContext()) {
                for (datagram in clientSocket.incoming) {
                    incoming.emit(datagram.packet.readByteArray())
                }
            }
        } catch (ex: Exception) {
            throw PortException("Failed to start UDP port for remote $remoteHost:$remotePort", ex)
        }
    }

    override suspend fun disconnect() {
        readJob?.cancel()
        socket?.close()
    }

    override suspend fun send(data: ByteArray) {
        if (!isConnected) throw PortClosedException("Port $this is not connected.")
        val socket = this.socket ?: throw PortClosedException("Port $this is not connected.")
        try {
            val packet = ByteReadPacket(data)
            socket.send(Datagram(packet, InetSocketAddress(remoteHost, remotePort)))
        } catch (ex: Exception) {
            throw PortException("Failed to send UDP datagram from $this", ex)
        }
    }

    override fun receive(): Flow<ByteArray> = incoming.asSharedFlow()

    override fun close() {
        val forceClose = meta["forceClose"].boolean ?: false
        if (forceClose) {
            socket?.dispose()
        } else {
            runBlocking { disconnect() }
        }
        scope.cancel("Port $this closed.")
    }

    override fun toString(): String = "port[udp:$remoteHost:$remotePort]"

    /**
     * A factory for creating [KtorUdpPort] instances.
     */
    public companion object : Factory<ConnectablePort> {
        override fun build(context: Context, meta: Meta): ConnectablePort {
            val remoteHost = meta["remoteHost"].string ?: error("Remote host for UDP port is not defined.")
            val remotePort = meta["remotePort"].int ?: error("Remote port for UDP port is not defined.")
            val localHost = meta["localHost"].string
            val localPort = meta["localPort"].int
            return KtorUdpPort(
                context = context,
                remoteHost = remoteHost,
                remotePort = remotePort,
                localHost = localHost,
                localPort = localPort,
                meta = meta
            ) {}
        }
    }
}