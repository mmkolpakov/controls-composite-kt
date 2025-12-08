package space.kscience.controls.composite.model.contracts.transformer

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta

/**
 * A contract for the runtime logic that performs a transformation between property types.
 * This is a **non-serializable**, functional interface.
 *
 * @param S The source property type (contravariant).
 * @param T The target property type (covariant).
 */
public fun interface PropertyTransformer<in S, out T> {
    /**
     * Performs the transformation.
     * @param source The value from the source property.
     * @return The transformed value suitable for the target property.
     */
    public suspend fun transform(source: S?): T?
}

/**
 * A factory for creating instances of [PropertyTransformer] logic at runtime.
 */
public interface PropertyTransformerFactory : Factory<PropertyTransformer<*, *>> {
    /**
     * The unique type identifier that this factory handles. Must match the `type` field of a `PropertyTransformerDescriptor`.
     */
    public val type: String

    /**
     * Creates an instance of [PropertyTransformer] based on the configuration from the descriptor.
     */
    override fun build(context: Context, meta: Meta): PropertyTransformer<*, *>
}
