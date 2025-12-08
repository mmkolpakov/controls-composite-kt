package space.kscience.controls.composite.model.common

import kotlinx.serialization.Serializable

/**
 * Represents a text string that can be localized on the client side.
 *
 * @property key The unique translation key (e.g., "device.motor.temp.desc").
 *               Used to look up the translation in a resource bundle.
 * @property default The fallback text to display if the key is not found or no bundle is available.
 * @property args Optional arguments for parameterized messages (e.g., "Value {0} exceeds limit {1}").
 */
@Serializable
public data class LocalizedText(
    val key: String,
    val default: String = key,
    val args: List<String> = emptyList()
) {
    override fun toString(): String = default

    public companion object {
        /**
         * Creates a simple non-localized text (key is absent or same as default).
         */
        public fun raw(text: String): LocalizedText = LocalizedText(text, text)
    }
}

/**
 * Helper to create [LocalizedText] from a simple string.
 * Treats the string as a raw, non-localized value by default to maintain backward compatibility.
 */
public fun String.asText(): LocalizedText = LocalizedText.raw(this)

/**
 * Helper to create a localized key.
 */
public fun i18n(key: String, default: String, vararg args: Any): LocalizedText =
    LocalizedText(key, default, args.map { it.toString() })
