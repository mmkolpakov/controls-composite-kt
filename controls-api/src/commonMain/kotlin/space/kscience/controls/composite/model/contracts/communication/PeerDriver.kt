package space.kscience.controls.composite.model.contracts.communication

import space.kscience.controls.composite.model.contracts.PeerConnection
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A factory responsible for creating an instance of a [space.kscience.controls.composite.model.contracts.PeerConnection].
 * This allows for pluggable peer connection implementations (e.g., for different protocols or transports).
 * Each driver implementation must have a unique [id].
 *
 * @param P The type of the peer connection this driver creates.
 */
public interface PeerDriver<out P : PeerConnection> {
    /**
     * A unique identifier for this driver implementation. This ID is referenced by a
     * [space.kscience.controls.composite.model.specs.PeerBlueprintDeclaration] to link a declarative configuration
     * to its runtime logic. For example: "magix.rsocket.tcp".
     */
    public val id: String

    /**
     * Creates a new peer connection instance based on the provided configuration.
     * @param context The DataForge context for the connection.
     * @param meta The configuration meta for the connection.
     * @return A new instance of the peer connection.
     */
    public fun create(context: Context, meta: Meta): P
}
