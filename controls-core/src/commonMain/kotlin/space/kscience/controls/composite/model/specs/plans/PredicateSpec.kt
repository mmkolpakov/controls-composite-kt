package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.names.Name

/**
 * A serializable representation of a predicate for a conditional action.
 */
@Serializable(with = PredicateSpec.Serializer::class)
public class PredicateSpec : Scheme() {
    /**
     * The address of the device on which to evaluate the predicate.
     */
    public var address: Address by requiredAddress()

    /**
     * The name of the boolean property to check.
     */
    public var propertyName: Name by requiredName()

    /**
     * The value the property is expected to have for the condition to be true.
     */
    public var expectedValue: Boolean by boolean(true)

    public companion object : SchemeSpec<PredicateSpec>(::PredicateSpec)
    public object Serializer : SchemeAsMetaSerializer<PredicateSpec>(Companion)
}
