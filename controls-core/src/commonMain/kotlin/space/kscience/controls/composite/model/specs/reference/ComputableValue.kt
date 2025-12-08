package space.kscience.controls.composite.model.specs.reference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A declarative, serializable representation of a value that can either be a literal
 * or a reference to another value that must be resolved at runtime.
 *
 * This sealed interface makes the distinction between static and dynamic values explicit
 * in device blueprints and transaction plans. The runtime is responsible for resolving a [ComputableValue]
 * into a concrete [Meta] before an operation is executed.
 */
@Serializable
public sealed interface ComputableValue : MetaRepr {
    /**
     * The fully qualified name of the value's type (e.g., "kotlin.Double").
     * This is essential for compile-time and static analysis tools to validate type compatibility
     * in property bindings without needing to resolve the value.
     */
    public val valueTypeName: String

    /**
     * An optional, unique identifier for the `kotlinx.serialization.KSerializer` to be used for this type.
     * This allows the runtime to bypass reflection-based serializer lookup, which is essential for
     * multiplatform compatibility (especially JS/Native).
     */
    public val serializerId: String?
}

/**
 * Represents a literal, static value.
 * The runtime can use this value directly without any resolution step.
 *
 * @property value The constant [Meta] value.
 * @property valueTypeName The fully qualified name of the literal value's type.
 * @property serializerId An optional, unique identifier for the serializer.
 */
@Serializable
@SerialName("literal")
public data class LiteralValue(
    val value: Meta,
    override val valueTypeName: String,
    override val serializerId: String? = null,
) : ComputableValue {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Represents a value that must be computed by resolving a reference at runtime.
 *
 * @property spec The declarative [ReferenceSpec] describing the source of the value and any
 *                transformations to be applied.
 * @property valueTypeName The fully qualified name of the referenced value's type.
 * @property serializerId An optional, unique identifier for the serializer.
 */
@Serializable
@SerialName("ref")
public data class ReferenceValue(
    val spec: ReferenceSpec,
    override val valueTypeName: String,
    override val serializerId: String? = null,
) : ComputableValue {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
