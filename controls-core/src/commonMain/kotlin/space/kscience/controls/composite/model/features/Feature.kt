package space.kscience.controls.composite.model.features

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A base interface for a Feature descriptor. A feature provides structured, serializable metadata
 * about a specific capability of a device.
 *
 * This is an open, non-sealed interface annotated with `@Polymorphic` to allow users of the library
 * to define their own custom features in separate modules, ensuring the framework is extensible.
 */
@Polymorphic
public interface Feature : MetaRepr {
    /**
     * A fully qualified name of the capability interface this feature describes.
     * For example, `space.kscience.controls.composite.model.contracts.device.Device`.
     */
    public val capability: String
}

/**
 * A universal feature implementation for rapid prototyping or plugins that do not require
 * a strict schema in the model. It allows passing arbitrary configuration via [Meta].
 *
 * @property capability The identifier of the capability (interface or service) this feature provides.
 * @property config The arbitrary configuration for this feature.
 */
@Serializable
@SerialName("feature.custom")
public data class CustomFeature(
    override val capability: String,
    val config: Meta = Meta.EMPTY
) : Feature {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
