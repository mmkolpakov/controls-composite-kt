package space.kscience.controls.composite.model.specs.bindings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.bindings.PropertyBinding
import space.kscience.controls.composite.model.meta.requiredMeta
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.scheme

/**
 * Binds a child's property to a constant [Meta] value.
 */
@SerialName("const")
@Serializable(with = ConstPropertyBinding.Serializer::class)
public class ConstPropertyBinding : Scheme(), PropertyBinding {
    /**
     * A type-safe reference to the property on the child device to be set.
     */
    override var target: PropertyReference by scheme(PropertyReference)

    /**
     * The constant [Meta] value to set.
     */
    public var value: Meta by requiredMeta()

    public companion object : SchemeSpec<ConstPropertyBinding>(::ConstPropertyBinding)
    public object Serializer : SchemeAsMetaSerializer<ConstPropertyBinding>(Companion)
}
