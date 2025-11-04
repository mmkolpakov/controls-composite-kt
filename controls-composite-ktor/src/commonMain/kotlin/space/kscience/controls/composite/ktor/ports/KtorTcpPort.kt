package space.kscience.controls.composite.ktor.ports

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import space.kscience.controls.composite.ports.ConnectablePort
import space.kscience.controls.composite.ports.PortClosedException
import space.kscience.controls.composite.ports.PortException
import space.kscience.dataforge.context.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string

/**
 * A [Port] implementation for TCP client sockets based on Ktor.
 * This port must be explicitly connected using the [connect] method before use.
 * Its lifecycle is managed externally, typically by a `DeviceDriver`.
 */
public class KtorTcpPort internal constructor(
    public val context: Context,
    private val host: String,
    private val port: Int,
    private val meta: Meta,
    private val socketOptions: SocketOptions.TCPClientSocketOptions.() -> Unit,
) : ConnectablePort {

    private val scope = CoroutineScope(context.coroutineContext + SupervisorJob(context.coroutineContext[Job]))

    private var socket: Socket? = null
    private var writeChannel: ByteWriteChannel? = null
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
            val clientSocket = aSocket(selectorManager).tcp().connect(host, port, socketOptions)
            this.socket = clientSocket
            this.writeChannel = clientSocket.openWriteChannel(autoFlush = true)
            val readChannel = clientSocket.openReadChannel()

            readJob = scope.launch(currentCoroutineContext()) {
                val buffer = ByteArray(1024)
                while (isActive) {
                    try {
                        val count = readChannel.readAvailable(buffer)
                        if (count > 0) {
                            incoming.emit(buffer.copyOf(count))
                        } else if (count < 0) {
                            // End of stream
                            cancel("TCP channel closed by remote.")
                        }
                    } catch (ex: Exception) {
                        if (ex !is CancellationException) {
                            context.logger.error(ex) { "Error while reading from TCP port $this" }
                        }
                        cancel("TCP port read failed", ex)
                    }
                }
            }
        } catch (ex: Exception) {
            throw PortException("Failed to connect TCP port to $host:$port", ex)
        }
    }

    override suspend fun disconnect() {
        readJob?.cancel()
        writeChannel?.cancel(CancellationException("Port $this disconnected."))
        socket?.close()
        // Do not cancel the whole scope, just the jobs
    }

    override suspend fun send(data: ByteArray) {
        if (!isConnected) throw PortClosedException("Port $this is not connected.")
        val channel = writeChannel ?: throw PortClosedException("Port $this is not connected.")
        try {
            channel.writeFully(data, 0, data.size)
        } catch (ex: Exception) {
            throw PortException("Failed to write to TCP port $this", ex)
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

    override fun toString(): String = "port[ktor.tcp:$host:$port]"

    public companion object : Factory<ConnectablePort> {
        override fun build(context: Context, meta: Meta): ConnectablePort {
            val host = meta["host"].string ?: "localhost"
            val port = meta["port"].int ?: error("Port value for TCP port is not defined in $meta")
            return KtorTcpPort(context, host, port, meta) {}
        }
    }
}