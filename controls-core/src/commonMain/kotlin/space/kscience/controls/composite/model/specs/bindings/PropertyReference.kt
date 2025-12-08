package space.kscience.controls.composite.model.specs.bindings

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.Name

/**
 * A type-safe, serializable reference to a specific device property, including its type information.
 */
@Serializable(with = PropertyReference.Serializer::class)
public class PropertyReference : Scheme() {
    /**
     * The local name of the property within its device.
     */
    public var propertyName: Name by requiredName()

    /**
     * The fully qualified name of the property's value type (e.g., "kotlin.Double").
     */
    public var valueTypeName: String by requiredString()

    /**
     * An optional, unique identifier for the `kotlinx.serialization.KSerializer` to be used for this type.
     */
    public var serializerId: String? by string()

    public companion object : SchemeSpec<PropertyReference>(::PropertyReference)
    public object Serializer : SchemeAsMetaSerializer<PropertyReference>(Companion)
}
