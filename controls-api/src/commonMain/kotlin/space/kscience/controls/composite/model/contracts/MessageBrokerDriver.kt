package space.kscience.controls.composite.model.contracts

import space.kscience.controls.composite.model.contracts.communication.MessageBroker
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A factory responsible for creating an instance of a [MessageBroker].
 * This allows for pluggable message bus implementations (e.g., in-memory, Magix, MQTT).
 * Each driver implementation must have a unique [id].
 *
 * @param B The type of the message broker this driver creates.
 */
public interface MessageBrokerDriver<T, out B : MessageBroker<T>> {
    /**
     * A unique identifier for this driver implementation. This ID is referenced by a
     * [space.kscience.controls.composite.model.specs.MessageBrokerBlueprintDeclaration] to link a declarative
     * configuration to its runtime logic. For example: "magix.rsocket.tcp".
     */
    public val id: String

    /**
     * Creates a new message broker instance based on the provided configuration.
     * @param context The DataForge context for the broker.
     * @param meta The configuration meta for the broker.
     * @return A new instance of the message broker.
     */
    public fun create(context: Context, meta: Meta): B
}
