package space.kscience.controls.composite.old.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.meta.toMeta

/**
 * A shared, lazily-initialized Json instance configured with all necessary polymorphic serializers
 * for the controls-composite old.
 *
 * This instance should be used for all conversions between `@Serializable` objects and [Meta] to ensure
 * consistency and correct handling of sealed interfaces like `DeviceMessage`, `ActionSpec`, etc.
 *
 * Using `by lazy` prevents initialization order issues and ensures the Json object is created
 * only when first needed.
 */
@OptIn(ExperimentalSerializationApi::class)
public val controlsJson: Json by lazy {
    Json {
        serializersModule = ControlsCompositeSerializersModule
        ignoreUnknownKeys = false
        prettyPrint = true
        classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
    }
}

/**
 * A generic factory for [MetaConverter] that leverages `kotlinx.serialization` for any `@Serializable` class.
 * It uses the centrally configured [controlsJson] instance to handle polymorphic types correctly.
 * This is the primary tool for bridging the strongly-typed serializable old with the dynamic Meta old.
 *
 * This function is the core implementation that accepts an explicit KSerializer.
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

/**
 * Creates a [MetaConverter] for a `@Serializable` type [T] by inferring its serializer.
 * This is a convenience extension for simple, non-generic serializable classes.
 *
 * For complex generic types (like `List<T>` or `Map<K, V>`), use the overload that accepts
 * an explicit `KSerializer`.
 *
 * @see serializable
 */
public inline fun <reified T> MetaConverter.Companion.serializable(): MetaConverter<T> =
    serializableMetaConverter(serializer<T>())

/**
 * Creates a [MetaConverter] for any type [T] using an explicit [KSerializer].
 * This is the correct way to handle complex generic types that require manually
 * constructed serializers.
 *
 * **This function solves the problem in `AnalyticalActions.kt`**.
 *
 * Example usage:
 * ```
 * val mapSerializer = MapSerializer(String.serializer(), Int.serializer())
 * val mapConverter = MetaConverter.serializable(mapSerializer)
 * ```
 */
public fun <T> MetaConverter.Companion.serializable(serializer: KSerializer<T>): MetaConverter<T> =
    serializableMetaConverter(serializer)