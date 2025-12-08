package space.kscience.controls.composite.model.serialization

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
 * A local Json instance for Core operations, unaware of higher-level modules.
 * This breaks the circular dependency between Core serialization and API/Spec serialization.
 */
@OptIn(ExperimentalSerializationApi::class)
public val coreJson: Json by lazy {
    Json {
        serializersModule = coreSerializersModule
        ignoreUnknownKeys = true
        prettyPrint = true
        classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
    }
}

/**
 * Converts a `@Serializable` object to a [Meta] representation using an efficient, in-memory bridge via `JsonElement`.
 * This helper is placed in Core to allow basic data structures (like DeviceFault) to implement [space.kscience.dataforge.meta.MetaRepr].
 */
public fun <T> serializableToMeta(
    serializer: KSerializer<T>,
    obj: T,
    json: Json = coreJson
): Meta {
    val jsonElement = json.encodeToJsonElement(serializer, obj)
    return jsonElement.toMeta()
}

/**
 * A generic factory for [MetaConverter] that leverages `kotlinx.serialization` for any `@Serializable` class.
 * It uses the centrally configured [coreJson] instance to handle polymorphic types correctly.
 */
private fun <T> serializableMetaConverter(serializer: KSerializer<T>): MetaConverter<T> = object : MetaConverter<T> {
    override fun readOrNull(source: Meta): T? {
        return try {
            val jsonElement = source.toJson()
            coreJson.decodeFromJsonElement(serializer, jsonElement)
        } catch (e: Exception) {
            null
        }
    }

    override fun convert(obj: T): Meta {
        return serializableToMeta(serializer, obj, coreJson)
    }
}

/**
 * Creates a [MetaConverter] for a `@Serializable` type [T] by inferring its serializer.
 */
internal inline fun <reified T> MetaConverter.Companion.serializable(): MetaConverter<T> =
    serializableMetaConverter(serializer<T>())

/**
 * Creates a [MetaConverter] for any type [T] using an explicit [KSerializer].
 */
public fun <T> MetaConverter.Companion.serializable(serializer: KSerializer<T>): MetaConverter<T> =
    serializableMetaConverter(serializer)
