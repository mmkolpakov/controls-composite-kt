package space.kscience.controls.composite.model.specs.bindings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.bindings.PropertyBinding
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.*

/**
 * Binds a child's property to a dynamic value from a source via a [ComputableValue].
 */
@SerialName("source")
@Serializable(with = SourcePropertyBinding.Serializer::class)
public class SourcePropertyBinding : Scheme(), PropertyBinding {
    /**
     * A type-safe reference to the property on the child device to be updated.
     */
    override var target: PropertyReference by scheme(PropertyReference)

    /**
     * The declarative specification of the reference to the source value.
     */
    public var source: ComputableValue by requiredSerializable()

    public companion object : SchemeSpec<SourcePropertyBinding>(::SourcePropertyBinding)
    public object Serializer : SchemeAsMetaSerializer<SourcePropertyBinding>(Companion)
}
