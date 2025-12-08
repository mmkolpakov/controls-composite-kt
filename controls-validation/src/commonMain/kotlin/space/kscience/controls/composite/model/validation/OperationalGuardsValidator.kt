package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.services.BlueprintRegistry
import space.kscience.controls.composite.model.features.GuardSpec
import space.kscience.controls.composite.model.features.OperationalGuardsFeature
import space.kscience.controls.composite.model.features.TimedPredicateGuardSpec
import space.kscience.controls.composite.model.features.ValueChangeGuardSpec
import space.kscience.controls.composite.model.meta.PropertyKind
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A validator for the [OperationalGuardsFeature]. It checks if the predicate properties
 * referenced by the guards actually exist on the blueprint and are of the correct kind (PREDICATE).
 */
public class OperationalGuardsValidator : FeatureValidator<OperationalGuardsFeature> {
    override fun validate(
        path: Name,
        declaration: DeviceBlueprintDeclaration,
        feature: OperationalGuardsFeature,
        registry: BlueprintRegistry,
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        feature.guards.forEach { guard: GuardSpec ->
            val predicateName = when (guard) {
                is TimedPredicateGuardSpec -> guard.predicateName
                is ValueChangeGuardSpec -> guard.propertyName
            }

            val predicateSpec = declaration.properties.find { it.name == predicateName }
            if (predicateSpec == null) {
                errors.add(
                    ValidationError.InvalidGuard(
                        path,
                        declaration.id.value,
                        predicateName,
                        "Predicate property not found on the blueprint."
                    )
                )
            } else if (guard is TimedPredicateGuardSpec && predicateSpec.kind != PropertyKind.PREDICATE) {
                // Only TimedPredicateGuardSpec strictly requires a PREDICATE kind.
                errors.add(
                    ValidationError.InvalidGuard(
                        path,
                        declaration.id.value,
                        predicateName,
                        "Property used in a 'timedPredicate' guard is not of kind PREDICATE."
                    )
                )
            }
        }
        return errors
    }
}

/**
 * The factory for [OperationalGuardsValidator].
 */
internal object OperationalGuardsValidatorFactory : FeatureValidatorFactory {
    override val capability: String get() = OperationalGuardsFeature.CAPABILITY

    override fun build(context: Context, meta: Meta): FeatureValidator<*> = OperationalGuardsValidator()
}
