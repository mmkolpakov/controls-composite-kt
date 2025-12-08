package space.kscience.controls.composite.model.validation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.meta.Value

/**
 * A sealed interface for a serializable, declarative validation rule for a device property.
 * Each implementation of this interface represents a specific type of constraint that can be
 * checked statically (by analysis tools or UIs) and/or enforced by the runtime.
 *
 * This formal, serializable model for validation is a key part of the framework's commitment
 * to creating predictable and verifiable device blueprints.
 */
@Serializable
public sealed interface ValidationRuleDescriptor : MetaRepr

/**
 * A validation rule that constrains a comparable value to a minimum and/or maximum bound.
 * The bounds are stored as `Value` to support serialization and different comparable types.
 *
 * @property min The inclusive minimum allowed value. If null, there is no lower bound.
 * @property max The inclusive maximum allowed value. If null, there is no upper bound.
 */
@Serializable
@SerialName("validation.range")
public data class RangeRuleDescriptor(
    val min: Value? = null,
    val max: Value? = null,
) : ValidationRuleDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}


/**
 * A validation rule that ensures a string value matches a given regular expression.
 *
 * @property pattern The regular expression pattern that the string must match.
 */
@Serializable
@SerialName("validation.regex")
public data class RegexRuleDescriptor(val pattern: String) : ValidationRuleDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A validation rule that ensures a string or collection has a minimum length.
 *
 * @property length The minimum required length.
 */
@Serializable
@SerialName("validation.minLength")
public data class MinLengthRuleDescriptor(val length: Int) : ValidationRuleDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A validation rule that delegates the validation logic to a named, reusable predicate
 * registered in the runtime. This allows for complex, domain-specific validation logic
 * to be implemented and tested independently, and then referenced declaratively.
 *
 * @property predicateId The unique identifier of the validation predicate to be resolved by the runtime.
 * @property meta Optional configuration metadata to be passed to the predicate logic.
 */
@Serializable
@SerialName("validation.custom")
public data class CustomPredicateRuleDescriptor(val predicateId: String, val meta: Meta = Meta.EMPTY) : ValidationRuleDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
