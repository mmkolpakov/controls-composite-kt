package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.meta.PropertyKind
import space.kscience.dataforge.names.Name

/**
 * Performs core validation checks on a blueprint declaration that are not tied to a specific feature.
 * This includes checking the consistency of action preconditions (required predicates).
 *
 * @param path The hierarchical path to the device being validated.
 * @param declaration The blueprint declaration to validate.
 * @return A list of validation errors. An empty list indicates no core errors were found.
 */
internal fun validateCoreBlueprint(path: Name, declaration: DeviceBlueprintDeclaration): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()

    // Validate action requirements (preconditions)
    declaration.actions.forEach { actionSpec ->
        actionSpec.requiredPredicates.forEach { predicateName ->
            val predicatePropertySpec = declaration.properties.find { it.name == predicateName }
            if (predicatePropertySpec == null) {
                errors.add(
                    ValidationError.InvalidActionRequirement(
                        path,
                        declaration.id.value,
                        actionSpec.name,
                        predicateName,
                        "Required predicate property not found on the blueprint."
                    )
                )
            } else if (predicatePropertySpec.kind != PropertyKind.PREDICATE) {
                errors.add(
                    ValidationError.InvalidActionRequirement(
                        path,
                        declaration.id.value,
                        actionSpec.name,
                        predicateName,
                        "Property is not of kind PREDICATE."
                    )
                )
            }
        }
    }
    return errors
}
