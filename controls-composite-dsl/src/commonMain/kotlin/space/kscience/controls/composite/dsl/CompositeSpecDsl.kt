package space.kscience.controls.composite.dsl

import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.DeviceBlueprint
import space.kscience.dataforge.context.Context

/**
 * Creates a [DeviceBlueprint] using a [DeviceSpecification] object.
 * This approach is recommended for creating reusable, well-structured, and easily testable device definitions.
 * The blueprint ID is taken from the specification's `id` property, or derived from its class name if not provided.
 *
 * @param D The type of the device contract.
 * @param spec The specification object to use.
 * @param context The context for creating the blueprint.
 * @return A fully configured and validated [DeviceBlueprint].
 */
public fun <D : Device> compositeDevice(
    spec: DeviceSpecification<D>,
    context: Context,
): DeviceBlueprint<D> {
    val builder = CompositeSpecBuilder<D>(context)
    spec.apply(builder)
    val id = spec.id ?: spec::class.simpleName ?: error("Cannot determine blueprint ID for anonymous specification.")
    return builder.build(id)
}

/**
 * Creates a [DeviceBlueprint] directly from a DSL configuration block, without needing a separate [DeviceSpecification] class.
 * This is useful for defining simple or one-off blueprints, for example in scripts or tests.
 *
 * This function is named in lowercase to follow Kotlin's naming conventions for factory functions and avoid confusion
 * with the [DeviceBlueprint] interface constructor.
 *
 * @param D The type of the device contract.
 * @param id The unique identifier for this blueprint.
 * @param context The context for creating the blueprint.
 * @param block The DSL configuration block.
 * @return A fully configured and validated [DeviceBlueprint].
 * @see compositeDevice for creating blueprints from reusable specification classes.
 */
public fun <D : Device> deviceBlueprint(
    id: String,
    context: Context,
    block: CompositeSpecBuilder<D>.() -> Unit,
): DeviceBlueprint<D> = CompositeSpecBuilder<D>(context).apply(block).build(id)