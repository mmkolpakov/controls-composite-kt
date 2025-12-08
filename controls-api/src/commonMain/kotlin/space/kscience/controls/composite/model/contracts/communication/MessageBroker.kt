package space.kscience.controls.composite.model.contracts.communication

import kotlinx.coroutines.flow.Flow
import space.kscience.controls.composite.model.contracts.communication.BrokerEvent
import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A contract for a message bus supporting publish-subscribe patterns.
 *
 * ### Topic Matching
 * Topic patterns can include wildcards to subscribe to multiple topics:
 * - `*` (asterisk): Matches a single token in the topic `Name`.
 * - `**` (double asterisk): Matches zero or more tokens at the end of a topic `Name`.
 *
 * **Examples:**
 * - `a.b.c`: Matches only the exact topic `a.b.c`.
 * - `a.*.c`: Matches `a.x.c`, `a.y.c`, but not `a.b.x.c`.
 * - `a.b.**`: Matches `a.b`, `a.b.c`, `a.b.c.d`, etc.
 */
public interface MessageBroker<T> {
    /**
     * Publishes a message to a specific topic.
     *
     * @param topic The hierarchical topic name to publish to.
     * @param message The [T] to send.
     * @param key An optional key for the message. In systems like Kafka, this key is used to determine the partition,
     *            ensuring that all messages with the same key are processed in order by a single consumer.
     * @param headers Additional transport-level metadata. This is the primary mechanism for propagating
     *                cross-cutting concerns like distributed tracing context (e.g., OpenTelemetry traceparent).
     */
    public suspend fun publish(
        topic: Name,
        message: T,
        key: String? = null,
        headers: Meta = Meta.EMPTY
    )

    /**
     * Subscribes to a topic pattern.
     *
     * @param topicPattern The topic pattern to subscribe to (e.g., "device.motor1.**").
     * @return A **hot** [Flow] of [BrokerEvent]s matching the pattern. The flow provides not only the
     *         [DeviceMessage] but also its associated transport metadata like key and headers.
     */
    public fun subscribe(topicPattern: Name): Flow<BrokerEvent<T>>
}

public typealias DeviceMessageBroker = MessageBroker<DeviceMessage>