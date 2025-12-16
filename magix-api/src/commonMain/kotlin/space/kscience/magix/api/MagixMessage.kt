package space.kscience.magix.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import space.kscience.dataforge.names.Name

/**
 * A standardized, serializable message structure for the Magix bus, based on the original Waltz-Controls RFC.
 * It includes a flexible payload and a set of headers for routing, correlation, and security.
 *
 * @property format A string identifier for the format of the [payload]. This allows for polymorphic
 *                  payloads on the bus (e.g., "controls-kt", "tango", "epics").
 * @property payload The main content of the message, represented as a [JsonElement].
 * @property sourceEndpoint The unique identifier of the endpoint that sent the message. This field is mandatory.
 * @property targetEndpoint An optional identifier for a specific recipient. If null, the message is considered a broadcast.
 * @property topic An optional hierarchical topic for content-based routing, used by some brokers.
 * @property id An optional unique identifier for this specific message, useful for request-response correlation.
 * @property parentId An optional identifier of a message that this message is a response to or is related to.
 * @property user An optional [JsonElement] containing user information (e.g., principal, authentication token).
 */
@Serializable
public data class MagixMessage(
    val format: String,
    val payload: JsonElement,
    val sourceEndpoint: Name,
    val targetEndpoint: Name? = null,
    val topic: Name? = null,
    val id: String? = null,
    val parentId: String? = null,
    val user: JsonElement? = null,
)

/**
 * A helper to extract a username from the [MagixMessage.user] field.
 * It follows the convention that if `user` is an object, it looks for a "name" property.
 * If `user` is a primitive, it returns its content. Returns "@error" for arrays.
 *
 * @return The extracted username, or `null` if not present.
 */
public val MagixMessage.userName: String? get() = when(user){
    null, JsonNull -> null
    is JsonObject -> user.jsonObject["name"]?.jsonPrimitive?.content
    is JsonPrimitive -> user.content
    else -> "@error"
}
