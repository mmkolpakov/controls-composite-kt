package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.*

/**
 * A reusable, declarative specification for identifying and versioning a descriptor API.
 * This class provides a structured way to handle API versions, replacing hardcoded
 * strings and ensuring that version information is explicit and introspectable.
 *
 * It is intended to be composed into higher-level descriptors like [HubStateDescriptor].
 *
 * @property api A unique identifier for the API this descriptor conforms to.
 * @property version The semantic version of the API schema (e.g., "1.0.0").
 */
@Serializable
public data class ApiVersionSpec(
    /**
     * A unique string identifying the API. For example, "controls-hub-descriptor".
     */
    val api: String = DEFAULT.api,
    /**
     * The semantic version of the API schema. For example, "1.0.0".
     */
    val version: String = DEFAULT.version,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        /**
         * The default, immutable instance for the current version of the hub descriptor API.
         */
        public val DEFAULT: ApiVersionSpec by lazy {
            ApiVersionSpec(
                api = "controls-hub-descriptor",
                version = "1.0.0"
            )
        }
    }
}
