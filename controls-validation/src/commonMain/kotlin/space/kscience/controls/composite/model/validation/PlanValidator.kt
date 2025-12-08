package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.services.BlueprintRegistry
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.specs.plans.*
import space.kscience.controls.composite.model.specs.reference.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.names.plus
//TODO refactor better

/**
 * A specialized, rigorous validator for [TransactionPlan]s.
 * It performs static analysis of the plan structure against the device blueprint to ensure
 * type safety, scope integrity, and referential correctness.
 *
 * @property contextBlueprintId The ID of the blueprint where this plan is defined (Self).
 * @property contextDeclaration The full declaration of the blueprint (Self).
 * @property registry The registry used to resolve child/peer blueprints for cross-device validation.
 */
public class PlanValidator(
    private val contextBlueprintId: BlueprintId,
    private val contextDeclaration: DeviceBlueprintDeclaration,
    private val registry: BlueprintRegistry
) {

    /**
     * Validates a plan starting from its root action.
     *
     * @param plan The plan to validate.
     * @param contextPath A human-readable path describing the plan's location (e.g., "Action[init]").
     * @return A list of validation errors found. An empty list indicates a valid plan.
     */
    public fun validate(plan: TransactionPlan, contextPath: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        validateAction(plan.rootAction, ValidationScope(), contextPath, errors)
        return errors
    }

    private fun validateAction(
        action: PlanActionSpec,
        scope: ValidationScope,
        path: String,
        errors: MutableList<ValidationError>
    ) {
        TODO()
    }

    /**
     * Checks if an alarm exists in the current blueprint context.
     */
    private fun validateAlarmReference(alarmName: Name, path: String, errors: MutableList<ValidationError>) {
        TODO()
    }

    /**
     * Validates a [ComputableValue] against an expected data type and the current scope.
     *
     * @param value The value to check.
     * @param expectedType The expected [DataType] (if known/applicable). Null if check is not required (e.g. DeclareVariable).
     */
    private fun validateComputableValue(
        value: ComputableValue,
        expectedType: DataType?,
        scope: ValidationScope,
        path: String,
        errors: MutableList<ValidationError>
    ) {
        TODO()
    }

    private fun validateReference(
        spec: ReferenceSpec,
        scope: ValidationScope,
        path: String,
        errors: MutableList<ValidationError>
    ) {
        TODO()
    }

    /**
     * Resolves the blueprint definition for the target device specified in the address.
     * Recursively traverses the hierarchy using the registry.
     */
    private fun resolveTargetBlueprint(
        address: Address,
        errorContext: String,
        errors: MutableList<ValidationError>
    ): DeviceBlueprintDeclaration? {
        TODO()
    }
}
