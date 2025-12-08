package space.kscience.controls.composite.model.specs.bindings

import kotlinx.serialization.Polymorphic
import space.kscience.dataforge.meta.MetaRepr

/**
 * An interface representing a declarative binding of a child's property.
 */
@Polymorphic
public interface PropertyBinding : MetaRepr {
    /**
     * A type-safe reference to the property on the child device that will be set or updated.
     */
    public val target: PropertyReference
}
