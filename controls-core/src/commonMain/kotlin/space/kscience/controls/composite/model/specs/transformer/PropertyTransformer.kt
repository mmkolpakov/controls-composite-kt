package space.kscience.controls.composite.model.specs.transformer

import kotlinx.serialization.Polymorphic
import space.kscience.dataforge.meta.MetaRepr

/**
 * A serializable, declarative description of a transformation between property types.
 * This object is stored within a blueprint and contains all necessary configuration for the transformation.
 * The actual transformation logic is resolved at runtime by a `PropertyTransformerFactory`.
 *
 * This interface is polymorphic to allow defining both standard math blocks and custom,
 * user-defined transformations.
 */
@Polymorphic
public interface PropertyTransformerDescriptor : MetaRepr {
    /**
     * The unique type identifier for this transformer. It is used by the runtime to find the corresponding
     * `PropertyTransformerFactory` to create the transformation logic.
     */
    public val type: String

    /**
     * The fully qualified name of the expected input value's type.
     */
    public val inputValueTypeName: String

    /**
     * The fully qualified name of the output value's type.
     */
    public val outputValueTypeName: String
}
