package space.kscience.magix.rsocket

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.rsocket.kotlin.ktor.client.RSocketSupport
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixEndpointFactory

/**
 * A factory for creating an RSocket-based [MagixEndpoint] using WebSockets for transport.
 * This factory is designed to be discovered and used by DataForge plugins.
 * The endpoint is created in an unconnected state and must be explicitly connected via `endpoint.connect()`.
 */
public val RSocketWithWebSocketsEndpointFactory: MagixEndpointFactory = MagixEndpointFactory { context, meta ->
    val host = meta["host"].string ?: "localhost"
    val port = meta["port"].int ?: MagixEndpoint.DEFAULT_MAGIX_HTTP_PORT
    val path = meta["path"].string ?: "/rsocket"

    val client = HttpClient {
        install(WebSockets)
        install(RSocketSupport) {
            meta["rsocket"]?.let { rsocketMeta ->
                connector {
                    configureFromMeta(rsocketMeta)
                }
            }
        }
    }

    RSocketMagixEndpoint.viaHttp(context.coroutineContext, client) {
        url(scheme = "ws", host = host, port = port, path = path)
    }
}