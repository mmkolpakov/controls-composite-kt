package space.kscience.magix.rsocket

import io.rsocket.kotlin.core.RSocketConnectorBuilder
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.keepalive.KeepAlive
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.payload.metadata
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.long
import space.kscience.dataforge.meta.string
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * An internal extension function to configure an RSocketConnectorBuilder from a Meta object.
 */
public fun RSocketConnectorBuilder.configureFromMeta(rsocketMeta: Meta) {
    connectionConfig {
        rsocketMeta["keepalive"]?.let { keepaliveMeta ->
            val interval = keepaliveMeta["interval"].string?.let(Duration::parse) ?: 20.seconds
            val maxLifetime = keepaliveMeta["maxLifetime"].string?.let(Duration::parse) ?: 90.seconds
            keepAlive = KeepAlive(interval, maxLifetime)
        }

        rsocketMeta["payloadMimeType"]?.let { mimeMeta ->
            payloadMimeType = PayloadMimeType(
                data = mimeMeta["data"].string ?: WellKnownMimeType.ApplicationJson.text,
                metadata = mimeMeta["metadata"].string ?: WellKnownMimeType.MessageRSocketCompositeMetadata.text
            )
        }

        rsocketMeta["setupPayload"]?.let { setupMeta ->
            setupPayload {
                buildPayload {
                    setupMeta["data"].string?.let { data(it) }
                    setupMeta["metadata"].string?.let { metadata(it) }
                }
            }
        }
    }

    rsocketMeta["reconnect"]?.let { reconnectMeta ->
        val retries = reconnectMeta["retries"].long
        if (retries != null && retries > 0) {
            reconnectable(retries)
        }
    }
}