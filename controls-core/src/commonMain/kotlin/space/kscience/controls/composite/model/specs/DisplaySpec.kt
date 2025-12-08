@file:OptIn(DFExperimental::class)

package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.convertable
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.misc.DFExperimental

/**
 * A reusable, declarative specification for common display-related metadata.
 * This is consumed by UI components to render device properties appropriately.
 */
@Serializable(with = DisplaySpec.Serializer::class)
public class DisplaySpec : Scheme() {
    /**
     * A human-readable, descriptive text for the element.
     * Supports localization via [LocalizedText].
     */
    public var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    /**
     * A label or title for the element, shorter than description.
     * Supports localization.
     */
    public var title: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    /**
     * An optional string for grouping related elements in a user interface.
     * Example: "Motion Control", "Power Management".
     * Simple string grouping is usually sufficient, but could be localized on the client.
     */
    public var group: String? by string()

    /**
     * An optional string identifier for a visual icon.
     * Typically maps to a Material Design icon name or a custom SVG asset key.
     */
    public var icon: String? by string()

    /**
     * An integer order hint for sorting elements in the UI.
     * Lower numbers appear first.
     */
    public var order: Int? by convertable(MetaConverter.int)

    /**
     * A format string for rendering the value.
     *
     * Examples:
     * - `"%.2f"` : Standard C-style floating point with 2 decimals.
     * - `"{value} kg"` : Template style.
     * - `"#0.00"` : Java DecimalFormat style.
     *
     * The exact syntax depends on the UI implementation, but standard placeholders are encouraged.
     */
    public var format: String? by string()

    /**
     * Helper property to set description from a raw string.
     */
    public var descriptionString: String?
        get() = description?.default
        set(value) {
            description = value?.let { LocalizedText.raw(it) }
        }

    public companion object : SchemeSpec<DisplaySpec>(::DisplaySpec)
    public object Serializer : SchemeAsMetaSerializer<DisplaySpec>(Companion)
}
