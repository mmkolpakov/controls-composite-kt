package space.kscience.controls.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import space.kscience.controls.core.controlsCoreSerializersModule
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toMeta

internal val coreJson: Json by lazy {
    Json {
        serializersModule = controlsCoreSerializersModule
        ignoreUnknownKeys = true
    }
}

/**
 * Converts a `@Serializable` object to a [Meta] representation using the core JSON config.
 */
public fun <T> serializableToMeta(serializer: KSerializer<T>, obj: T): Meta {
    val jsonElement = coreJson.encodeToJsonElement(serializer, obj)
    return jsonElement.toMeta()
}