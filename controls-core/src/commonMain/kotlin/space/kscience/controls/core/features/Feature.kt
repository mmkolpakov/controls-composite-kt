package space.kscience.controls.core.features

import kotlinx.serialization.Polymorphic
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
     * For example, `space.kscience.controls.core.contracts.Device`.
     */
    public val capability: String
}