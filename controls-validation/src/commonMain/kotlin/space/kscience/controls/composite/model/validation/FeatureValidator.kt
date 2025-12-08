package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.services.BlueprintRegistry
import space.kscience.controls.composite.model.features.Feature
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.names.Name

/**
 * A contract for a component that implements validation logic for a specific type of [Feature].
 * This allows the validation system to be extended by different modules without modifying the core validator.
 *
 * @param F The specific type of [Feature] this validator can process. The type is contravariant (`in`)
 *          to allow a validator for a general feature to be used for its subtypes.
 */
public fun interface FeatureValidator<in F : Feature> {
    /**
     * Validates a specific feature instance within the context of its parent blueprint declaration.
     * The implementation should check for logical consistency, correctness of configuration, and
     * dependencies on other parts of the blueprint or other blueprints in the registry.
     *
     * @param path The hierarchical path to the device where this feature is declared (e.g. "root.child[arm]").
     *             This path should be included in any [ValidationError] generated.
     * @param declaration The [DeviceBlueprintDeclaration] being validated, which contains the feature.
     * @param feature The instance of the feature to validate.
     * @param registry The [BlueprintRegistry], which is required to validate dependencies on other blueprints
     *                 (e.g., checking properties of a child device's blueprint).
     * @return A list of [ValidationError]s found. An empty list signifies that the feature is valid.
     */
    public fun validate(
        path: Name,
        declaration: DeviceBlueprintDeclaration,
        feature: F,
        registry: BlueprintRegistry
    ): List<ValidationError>
}

/**
 * A factory for creating instances of [FeatureValidator]. This factory is discoverable by the
 * [FeatureValidatorRegistry] via the DataForge plugin system. Each factory is responsible for
 * a single type of feature, identified by its `capability` string.
 */
public interface FeatureValidatorFactory : Factory<FeatureValidator<*>> {
    /**
     * The unique capability string of the [Feature] that this factory's validator handles.
     * This must exactly match the `capability` property of the target feature.
     */
    public val capability: String
}
