package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.reference.ReferenceSpec
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A sealed interface for specifying how a property value is derived or transformed.
 * These specifications allow defining "Virtual Devices" or "Soft Signals" entirely within the blueprint,
 * enabling calculations like averages, scalings, or logical combinations without deploying new code.
 */
@Serializable
public sealed interface TransformationSpec : MetaRepr

/**
 * A simple linear scaling transformation: `y = (x * scale) + offset`.
 * Useful for converting raw ADC counts to physical units.
 */
@Serializable
@SerialName("transform.linear")
public data class LinearTransformation(
    val scale: Double = 1.0,
    val offset: Double = 0.0
) : TransformationSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A transformation defined by an expression string interpreted by a specific engine.
 *
 * @property expression The code or formula string (e.g., "a + b / 2").
 * @property engine The identifier of the expression engine (e.g., "kmath", "javascript", "spel").
 * @property dependencies A map of variable names used in the expression to their source [ReferenceSpec]s.
 *                        Example: `{"a": ReferenceSpec(...), "b": ReferenceSpec(...)}`.
 */
@Serializable
@SerialName("transform.expression")
public data class ExpressionTransformation(
    val expression: String,
    val engine: String = "kmath",
    val dependencies: Map<String, ReferenceSpec> = emptyMap()
) : TransformationSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A transformation that maps discrete input values to specific output values (Lookup Table).
 * Useful for mapping enum integers to status strings or vice versa.
 *
 * @property mapping A map where keys are string representations of input values and values are the outputs.
 * @property defaultValue The value to return if the input does not match any key. If null, the transformation may fail or return null.
 */
@Serializable
@SerialName("transform.map")
public data class MapTransformation(
    val mapping: Map<String, String>,
    val defaultValue: String? = null
) : TransformationSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A transformation that applies a moving average filter to a numeric input.
 *
 * @property windowSize The number of samples to include in the moving average.
 */
@Serializable
@SerialName("transform.movingAverage")
public data class MovingAverageTransformation(
    val windowSize: Int
) : TransformationSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A transformation that integrates a numeric value over time (Riemann sum).
 *
 * @property resetOnStart If true, the integral is reset to 0 when the device starts.
 */
@Serializable
@SerialName("transform.integration")
public data class IntegrationTransformation(
    val resetOnStart: Boolean = true
) : TransformationSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
