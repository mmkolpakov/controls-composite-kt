package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A pure, serializable, and platform-agnostic declaration of a message broker blueprint.
 * This data class contains all static configuration for a broker, but intentionally excludes
 * any executable logic like a `driver`.
 *
 * @property id A unique identifier for this blueprint declaration.
 * @property driverId A unique identifier for the `MessageBrokerDriver` implementation that the runtime
 *                    should use to create an instance of this broker.
 * @property meta Additional, driver-specific configuration metadata.
 */
@Serializable
public data class MessageBrokerBlueprintDeclaration(
    val id: String,
    val driverId: String,
    val meta: Meta = Meta.EMPTY
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
