package space.kscience.magix.rsocket

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.io.readString
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixMessage
import space.kscience.magix.api.MagixMessageFilter
import space.kscience.magix.api.filter
import kotlin.coroutines.CoroutineContext

/**
 * An implementation of [MagixEndpoint] that uses a connected RSocket instance as the transport layer.
 * This endpoint uses the Request-Stream old for subscriptions and Fire-and-Forget for broadcasting.
 *
 * @param coroutineContext The context for the Flow operations and internal jobs.
 * @param rsocketProvider A suspendable factory function to obtain a connected RSocket instance.
 */
public class RSocketMagixEndpoint private constructor(
    override val coroutineContext: CoroutineContext,
    private val rsocketProvider: suspend () -> RSocket,
) : MagixEndpoint, Closeable, CoroutineScope {

    private var rSocket: RSocket? = null

    /**
     * Establishes the connection to the RSocket server. This method is suspendable and must be called
     * before any other operations can be performed. It's safe to call multiple times.
     */
    public suspend fun connect() {
        if (rSocket == null || rSocket?.isActive == false) {
            rSocket = rsocketProvider()
        }
    }

    private fun requireRSocket(): RSocket = rSocket
        ?: error("RSocketMagixEndpoint is not connected. Call `connect()` before using.")

    override fun subscribe(filter: MagixMessageFilter): Flow<MagixMessage> {
//        val filterPayload = MagixEndpoint.magixJson.encodeToString(MagixMessageFilter.serializer(), filter)
//        val request = buildPayload {
//            data(filterPayload)
//        }
//        return requireRSocket().requestStream(request).map { payload ->
//            payload.use {
//                MagixEndpoint.magixJson.decodeFromString(
//                    MagixMessage.serializer(),
//                    it.data.readString()
//                )
//            }
//        }.filter(filter).flowOn(coroutineContext) // Ensure flow processing happens on the correct dispatcher.
        TODO("magixJson is deprecated")
    }

    override suspend fun broadcast(message: MagixMessage) {
//        val payload = buildPayload {
//            data(MagixEndpoint.magixJson.encodeToString(MagixMessage.serializer(), message))
//        }
//        requireRSocket().fireAndForget(payload)
        TODO("magixJson is deprecated")
    }

    override fun close() {
        rSocket?.cancel("RSocketMagixEndpoint is closed.")
        coroutineContext.cancel(CancellationException("RSocketMagixEndpoint is closed."))
    }

    public companion object {
        /**
         * Creates an RSocketMagixEndpoint using a generic RSocket provider.
         * The lifecycle of any resources used by the provider should be managed externally
         * or tied to the provided CoroutineContext.
         */
        public fun fromProvider(
            coroutineContext: CoroutineContext,
            rsocketProvider: suspend () -> RSocket,
        ): RSocketMagixEndpoint = RSocketMagixEndpoint(coroutineContext, rsocketProvider)


        /**
         * Creates an RSocketMagixEndpoint using an HttpClient for WebSocket transport.
         * The lifecycle of the provided HttpClient will be tied to the endpoint's CoroutineContext.
         */
        public fun viaHttp(
            coroutineContext: CoroutineContext,
            client: HttpClient,
            requestBuilder: HttpRequestBuilder.() -> Unit,
        ): RSocketMagixEndpoint {
            // The provider lambda captures the client and the builder.
            val rsocketProvider: suspend () -> RSocket = { client.rSocket(requestBuilder) }

            // Create the endpoint and link its lifecycle to the HttpClient.
            return fromProvider(coroutineContext, rsocketProvider).also { endpoint ->
                // When the endpoint's scope is cancelled, also close the client.
                endpoint.coroutineContext.job.invokeOnCompletion {
                    client.close()
                }
            }
        }
    }
}