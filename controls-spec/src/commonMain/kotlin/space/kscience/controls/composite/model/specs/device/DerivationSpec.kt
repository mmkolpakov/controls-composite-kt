package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.string

/**
 * Defines how a property value is derived (calculated) from other values.
 * This is declarative: the Model stores the formula, the Runtime/UI executes it.
 *
 * @property expression The formula string. The syntax depends on the engine (e.g., "tag('a') + 10").
 * @property engine The identifier of the expression engine (e.g., "kotlin-script", "simple-eval", "excel").
 */
@Serializable(with = DerivationSpec.Serializer::class)
public class DerivationSpec : Scheme() {
    public var expression: String? by string()
    public var engine: String by string { "simple" }

    public companion object : SchemeSpec<DerivationSpec>(::DerivationSpec)
    public object Serializer : SchemeAsMetaSerializer<DerivationSpec>(Companion)
}
