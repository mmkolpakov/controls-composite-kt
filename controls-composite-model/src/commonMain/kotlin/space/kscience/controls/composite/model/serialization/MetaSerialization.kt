package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.meta.toMeta

/**
 * A shared, lazily-initialized Json instance configured with all necessary polymorphic serializers
 * for the controls-composite model.
 *
 * This instance should be used for all conversions between `@Serializable` objects and [Meta] to ensure
 * consistency and correct handling of sealed interfaces like `DeviceMessage`, `ActionSpec`, etc.
 *
 * Using `by lazy` prevents initialization order issues and ensures the Json object is created
 * only when first needed.
 */
public val controlsJson: Json by lazy {
    Json {
        serializersModule = ControlsCompositeSerializersModule
        ignoreUnknownKeys = false
        prettyPrint = true
    }
}

/**
 * Converts a `@Serializable` object to a [Meta] representation using the centrally configured [controlsJson] bridge.
 * This is the idiomatic and recommended way to implement the [MetaRepr.toMeta] method for any serializable class
 * within this framework. It ensures that serialization logic is defined in a single place (via annotations) and
 * is consistently applied.
 *
 * @param serializer The explicit [KSerializer] for type [T].
 * @param obj The object to be converted.
 * @return The [Meta] representation of the object.
 */
public fun <T> serializableToMeta(serializer: KSerializer<T>, obj: T): Meta {
    val jsonElement = controlsJson.encodeToJsonElement(serializer, obj)
    return jsonElement.toMeta()
}

/**
 * A generic factory for [MetaConverter] that leverages `kotlinx.serialization` for any `@Serializable` class.
 * It uses the centrally configured [controlsJson] instance to handle polymorphic types correctly.
 * This is the primary tool for bridging the strongly-typed serializable model with the dynamic Meta model.
 *
 * @param T The serializable type to be converted.
 * @param serializer The explicit KSerializer for the type T.
 * @return A [MetaConverter] instance for the given type.
 */
public fun <T> serializableMetaConverter(serializer: KSerializer<T>): MetaConverter<T> = object : MetaConverter<T> {
    /**
     * Converts a [Meta] object back to a typed object [T].
     * This process involves an intermediate conversion to [kotlinx.serialization.json.JsonElement].
     *
     * @param source The [Meta] to be read.
     * @return The deserialized object of type [T], or null if the conversion fails.
     */
    override fun readOrNull(source: Meta): T? {
        return try {
            val jsonElement = source.toJson()
            controlsJson.decodeFromJsonElement(serializer, jsonElement)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Converts a typed object [T] to its [Meta] representation.
     *
     * @param obj The object to be converted.
     * @return The [Meta] representation.
     */
    override fun convert(obj: T): Meta {
        val jsonElement = controlsJson.encodeToJsonElement(serializer, obj)
        return jsonElement.toMeta()
    }
}