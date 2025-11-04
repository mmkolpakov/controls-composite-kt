package space.kscience.magix.rsocket

import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.transport.ktor.tcp.KtorTcpClientTransport
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixEndpointFactory

/**
 * Actual implementation of RSocket TCP endpoint factory for JVM and Native platforms.
 */
public actual val RSocketWithTcpEndpointFactory: MagixEndpointFactory = MagixEndpointFactory { context, meta ->
    val host = meta["host"].string ?: "localhost"
    val port = meta["port"].int ?: MagixEndpoint.DEFAULT_MAGIX_RAW_PORT

    // Create a Ktor TCP transport for RSocket, it is independent of HttpClient
    val transport = KtorTcpClientTransport(context.coroutineContext)

    // Create and configure an RSocketConnector
    val connector = RSocketConnector {
        meta["rsocket"]?.let { rsocketMeta ->
            configureFromMeta(rsocketMeta)
        }
    }

    // Create a provider lambda that will establish the connection when called
    val rsocketProvider = suspend {
        connector.connect(transport.target(host, port))
    }

    // Pass the provider to the generic RSocketMagixEndpoint factory
    RSocketMagixEndpoint.fromProvider(context.coroutineContext, rsocketProvider)
}