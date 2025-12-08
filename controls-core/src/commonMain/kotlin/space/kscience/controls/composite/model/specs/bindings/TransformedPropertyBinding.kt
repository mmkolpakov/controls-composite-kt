package space.kscience.controls.composite.model.specs.bindings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.bindings.PropertyBinding
import space.kscience.controls.composite.model.specs.transformer.PropertyTransformerDescriptor
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.scheme

/**
 * A binding that includes a serializable transformation descriptor.
 * The runtime uses the provided descriptor to find a `PropertyTransformerFactory` and create the
 * logic to convert the source value before applying it to the target.
 */
@SerialName("transformed")
@Serializable(with = TransformedPropertyBinding.Serializer::class)
public class TransformedPropertyBinding : Scheme(), PropertyBinding {
    /**
     * A type-safe reference to the property on the child device.
     */
    override var target: PropertyReference by scheme(PropertyReference)

    /**
     * The declarative specification of the reference to the source value.
     */
    public var source: ComputableValue by requiredSerializable()

    /**
     * The serializable descriptor that defines the value transformation.
     */
    public var transformer: PropertyTransformerDescriptor by requiredSerializable()

    public companion object : SchemeSpec<TransformedPropertyBinding>(::TransformedPropertyBinding)
    public object Serializer : SchemeAsMetaSerializer<TransformedPropertyBinding>(Companion)
}
