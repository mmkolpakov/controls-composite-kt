package space.kscience.controls.composite.magix

import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import space.kscience.controls.composite.model.Address
import space.kscience.controls.composite.model.ExecutionContext
import space.kscience.controls.composite.model.contracts.PeerConnectionException
import space.kscience.controls.composite.model.messages.BinaryDataRequest
import space.kscience.controls.composite.model.messages.BinaryReadyNotification
import space.kscience.controls.composite.model.services.PeerSignalingService
import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.io.Envelope
import space.kscience.dataforge.names.parseAsName
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.send
import space.kscience.magix.api.subscribe
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A service that uses a [MagixEndpoint] for **signaling** and coordination of peer-to-peer binary data transfers.
 * This implementation fulfills the [PeerSignalingService] contract using the Magix message bus.
 *
 * @property endpoint The underlying [MagixEndpoint] for communication.
 * @property localHubId The ID of the local hub, used to form the source [Address].
 */
public class MagixPeerSignalingService(
    private val endpoint: MagixEndpoint,
    private val localHubId: String,
): PeerSignalingService, AbstractPlugin() {
    override val tag: PluginTag get() = PeerSignalingService.tag

    override suspend fun requestBinaryData(
        address: Address,
        contentId: String,
        context: ExecutionContext,
        timeout: Duration,
    ): BinaryReadyNotification {
        val requestId = uuid4().toString()
        val requestMessage = BinaryDataRequest(
            time = Clock.System.now(),
            contentId = contentId,
            sourceDevice = Address(localHubId, context.principal.name.parseAsName()),
            targetDevice = address,
            requestId = requestId,
            correlationId = context.correlationId
        )

        endpoint.send(
            format = MagixMessageBroker.deviceMessageFormat,
            payload = requestMessage,
            source = localHubId,
            target = address.hubId
        )

        return withTimeout(timeout) {
            endpoint.subscribe(MagixMessageBroker.deviceMessageFormat)
                .map { it.second }
                .first { msg ->
                    (msg is BinaryReadyNotification) && (msg.requestId == requestId)
                } as BinaryReadyNotification
        }
    }

    override suspend fun notifyBinaryReady(
        targetAddress: Address?,
        contentId: String,
        envelope: Envelope,
        context: ExecutionContext,
    ) {
        val notification = BinaryReadyNotification(
            time = Clock.System.now(),
            contentId = contentId,
            contentMeta = envelope.meta,
            sourceDevice = Address(localHubId, context.principal.name.parseAsName()),
            targetDevice = targetAddress,
            correlationId = context.correlationId
        )

        endpoint.send(
            format = MagixMessageBroker.deviceMessageFormat,
            payload = notification,
            source = localHubId,
            target = targetAddress?.hubId
        )
    }
}