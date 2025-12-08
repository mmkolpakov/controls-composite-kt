package space.kscience.controls.composite.model.specs.transformer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.double

/**
 * A descriptor for a standard transformer that converts any object to its string representation.
 */
@SerialName("transformer.toString")
@Serializable(with = ToStringTransformerDescriptor.Serializer::class)
public class ToStringTransformerDescriptor : Scheme(), PropertyTransformerDescriptor {
    override val type: String get() = "toString"
    override val inputValueTypeName: String get() = "kotlin.Any"
    override val outputValueTypeName: String get() = "kotlin.String"

    public companion object : SchemeSpec<ToStringTransformerDescriptor>(::ToStringTransformerDescriptor)
    public object Serializer : SchemeAsMetaSerializer<ToStringTransformerDescriptor>(Companion)
}

/**
 * A descriptor for a standard transformer that applies a linear transformation (`y = kx + b`) to a numeric value.
 */
@SerialName("transformer.linear")
@Serializable(with = LinearTransformDescriptor.Serializer::class)
public class LinearTransformDescriptor : Scheme(), PropertyTransformerDescriptor {
    /**
     * The scaling factor (k).
     */
    public var scale: Double by double(1.0)

    /**
     * The offset (b).
     */
    public var offset: Double by double(0.0)

    override val type: String get() = "linear"
    override val inputValueTypeName: String get() = "kotlin.Number"
    override val outputValueTypeName: String get() = "kotlin.Double"

    public companion object : SchemeSpec<LinearTransformDescriptor>(::LinearTransformDescriptor)
    public object Serializer : SchemeAsMetaSerializer<LinearTransformDescriptor>(Companion)
}

/**
 * A descriptor for a polynomial transformation of arbitrary order.
 * `y = c0 + c1*x + c2*x^2 + ... + cn*x^n`
 *
 * @property coefficients The list of coefficients [c0, c1, c2, ... cn].
 *                        Index in the list corresponds to the power of x.
 */
@Serializable
@SerialName("transformer.polynomial")
public data class PolynomialTransformerDescriptor(
    val coefficients: List<Double>
) : PropertyTransformerDescriptor {
    override val type: String = "polynomial"
    override val inputValueTypeName: String = "kotlin.Number"
    override val outputValueTypeName: String = "kotlin.Double"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A descriptor for value mapping via a lookup table (interpolation).
 * Useful for calibrating non-linear sensors.
 *
 * @property points A map where key is the input value (X) and value is the output value (Y).
 * @property interpolate If true, the runtime should perform linear interpolation between points.
 *                       If false, it may perform nearest-neighbor or exact match lookup.
 */
@Serializable
@SerialName("transformer.lookup")
public data class LookupTableTransformerDescriptor(
    val points: Map<Double, Double>,
    val interpolate: Boolean = true
) : PropertyTransformerDescriptor {
    override val type: String = "lookup"
    override val inputValueTypeName: String = "kotlin.Number"
    override val outputValueTypeName: String = "kotlin.Double"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A descriptor for a chain of transformations applied sequentially.
 * The output of stage N becomes the input of stage N+1.
 *
 * @property stages The ordered list of transformers to apply.
 */
@Serializable
@SerialName("transformer.chain")
public data class ChainTransformerDescriptor(
    val stages: List<PropertyTransformerDescriptor>
) : PropertyTransformerDescriptor {
    override val type: String = "chain"
    // Input type is determined by the first stage
    override val inputValueTypeName: String get() = stages.firstOrNull()?.inputValueTypeName ?: "kotlin.Any"
    // Output type is determined by the last stage
    override val outputValueTypeName: String get() = stages.lastOrNull()?.outputValueTypeName ?: "kotlin.Any"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A universal descriptor for custom transformations not covered by standard types.
 *
 * @property transformationId The identifier of the custom transformation logic registered in the runtime.
 * @property config Arbitrary configuration parameters for the transformation.
 */
@Serializable
@SerialName("transformer.custom")
public data class CustomTransformerDescriptor(
    val transformationId: String,
    val config: Meta = Meta.EMPTY
) : PropertyTransformerDescriptor {
    override val type: String = "custom"
    override val inputValueTypeName: String = "kotlin.Any" // Loose typing for custom
    override val outputValueTypeName: String = "kotlin.Any"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
