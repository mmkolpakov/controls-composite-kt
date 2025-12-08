package space.kscience.controls.composite.model.contracts.communication

import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A container for a message retrieved from the broker, including transport-level metadata.
 * This makes the subscription abstraction "honest" by exposing all relevant information from the underlying
 * messaging system, which is crucial for features like ordered delivery and distributed tracing.
 *
 * @property topic The topic on which the message was received.
 * @property message The deserialized [space.kscience.controls.composite.model.messages.DeviceMessage].
 * @property key The optional key of the message, used for partitioning in systems like Kafka.
 * @property headers A [space.kscience.dataforge.meta.Meta] object containing transport-level headers, used for cross-cutting concerns.
 */
public data class BrokerEvent<out T>(
    val topic: Name,
    val message: T,
    val key: String? = null,
    val headers: Meta = Meta.EMPTY
)
