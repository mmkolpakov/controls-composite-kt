package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.device.PlanExecutorDevice
import space.kscience.controls.composite.model.contracts.device.StatefulDevice
import space.kscience.controls.composite.model.contracts.device.TaskExecutorDevice
import space.kscience.controls.composite.model.features.AlarmsFeature
import space.kscience.controls.composite.model.meta.PropertyKind
import space.kscience.controls.composite.model.services.BlueprintRegistry
import space.kscience.controls.composite.model.specs.bindings.ConstPropertyBinding
import space.kscience.controls.composite.model.specs.bindings.SourcePropertyBinding
import space.kscience.controls.composite.model.specs.bindings.TransformedPropertyBinding
import space.kscience.controls.composite.model.specs.device.DeviceBlueprintDeclaration
import space.kscience.controls.composite.model.specs.device.LocalChildComponentConfig
import space.kscience.controls.composite.model.specs.device.PlanLogic
import space.kscience.controls.composite.model.specs.device.RemoteChildComponentConfig
import space.kscience.controls.composite.model.specs.device.TaskLogic
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Laminate
import space.kscience.dataforge.meta.descriptors.validate
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus

/**
 * A utility object for validating [DeviceBlueprintDeclaration] instances.
 * It provides methods for both shallow (self-contained) and deep (recursive) validation,
 * including support for blueprint inheritance.
 */
public object CompositeSpecValidator {
    /**
     * Performs a "deep", recursive validation of a [DeviceBlueprintDeclaration] using services from the provided [context].
     *
     * @param declaration The blueprint declaration to validate.
     * @param context The [Context] containing necessary services like [BlueprintRegistry] and [FeatureValidatorRegistry].
     * @return A list of [ValidationError]. An empty list signifies a valid blueprint.
     */
    public fun validateWithContext(declaration: DeviceBlueprintDeclaration, context: Context): List<ValidationError> {
        // We need a blueprint registry to resolve dependencies (parents, children)
        val registry = context.plugins.find { it is BlueprintRegistry } as? BlueprintRegistry
            ?: error("BlueprintRegistry is not available in the context. Validation cannot proceed.")

        val planValidator = PlanValidator(
            contextBlueprintId = TODO(),
            contextDeclaration = TODO(),
            registry = TODO()
        )
        val cache = mutableMapOf<BlueprintId, List<ValidationError>>()

        return validateRecursively(
            Name.EMPTY,
            declaration.id,
            registry,
            context,
            planValidator,
            emptySet(),
            cache
        )
    }

    private fun validateRecursively(
        currentPath: Name,
        blueprintId: BlueprintId,
        registry: BlueprintRegistry,
        context: Context,
        planValidator: PlanValidator,
        validationChain: Set<BlueprintId>,
        cache: MutableMap<BlueprintId, List<ValidationError>>,
    ): List<ValidationError> {
        // --- Cycle Detection ---
        if (blueprintId in validationChain) {
            return listOf(ValidationError.CyclicalDependency(currentPath, (validationChain + blueprintId).toList()))
        }
        val newChain = validationChain + blueprintId

        // --- 1. Resolve and Merge the Blueprint ---
        val declaration = registry.findById(blueprintId)
            ?: return listOf(ValidationError.BlueprintNotFound(currentPath, Name.EMPTY, blueprintId))

        val (mergedDeclaration, parentErrors) = if (declaration.inheritsFrom != null) {
            val parentBlueprintId = declaration.inheritsFrom!!
            val parentDeclaration = registry.findById(parentBlueprintId)
                ?: return listOf(ValidationError.BlueprintNotFound(currentPath, Name.EMPTY, parentBlueprintId))

            // Recursively validate the parent first to ensure the base is solid
            val errors = validateRecursively(
                currentPath, // We are still at the same logical location, just checking dependencies
                parentBlueprintId,
                registry,
                context,
                planValidator,
                newChain,
                cache
            )

            if (errors.isNotEmpty()) {
                // If parent is invalid, stop here to avoid cascading errors on bad data
                declaration to errors
            } else {
                // If parent is valid, merge the declarations using Laminate.
                // Child overrides parent.
                val mergedMeta = Laminate(declaration.toMeta(), parentDeclaration.toMeta())
                val mergedScheme = DeviceBlueprintDeclaration.read(mergedMeta)
                mergedScheme to emptyList()
            }
        } else {
            declaration to emptyList()
        }

        if (parentErrors.isNotEmpty()) {
            return parentErrors
        }

        // --- 2. Validate the (potentially merged) blueprint itself ---
        val errors = mutableListOf<ValidationError>()
        errors.addAll(validateSelf(currentPath, mergedDeclaration))
        errors.addAll(validateCoreBlueprint(currentPath, mergedDeclaration))

        // --- 3. Feature-specific validation using pluggable validators ---
        // This allows external modules to inject custom validation logic
        val validatorRegistry = context.plugins.find { it is FeatureValidatorRegistry } as? FeatureValidatorRegistry

        if (validatorRegistry != null) {
            mergedDeclaration.features.values.forEach { feature ->
                validatorRegistry.getValidatorFor(feature)?.let { validator ->
                    errors.addAll(validator.validate(currentPath, mergedDeclaration, feature, registry))
                }
            }
        }

        // --- 4. Validate Lifecycle Plans ---
        // Lifecycle plans must be valid executable sequences
        mergedDeclaration.lifecycle.onAttach?.let {
            TODO()
        }
        mergedDeclaration.lifecycle.onStart?.let {
            TODO()
        }
        mergedDeclaration.lifecycle.onStop?.let {
            TODO()
        }
        mergedDeclaration.lifecycle.onDetach?.let {
            TODO()
        }

        // --- 5. Validate Action Logic Plans ---
        mergedDeclaration.actions.forEach { action ->
            val logic = action.logic
            if (logic is PlanLogic) {
                TODO()
            }
        }

        // --- 6. Validate Children's configuration and recursively validate their blueprints ---
        mergedDeclaration.children.forEach { (childName, childConfig) ->
            val childPath = currentPath + childName
            val childBlueprint = registry.findById(childConfig.blueprintId)

            if (childBlueprint == null) {
                errors.add(ValidationError.BlueprintNotFound(childPath, childName, childConfig.blueprintId))
            } else {
                when (childConfig) {
                    is LocalChildComponentConfig -> validateLocalChild(
                        childPath,
                        childName,
                        childConfig,
                        childBlueprint,
                        errors
                    )

                    is RemoteChildComponentConfig -> validateRemoteChild(
                        childPath,
                        mergedDeclaration,
                        childName,
                        childConfig,
                        errors
                    )
                }
                // Recursively validate the child's blueprint structure
                errors.addAll(
                    validateRecursively(
                        childPath,
                        childConfig.blueprintId,
                        registry,
                        context,
                        planValidator,
                        newChain,
                        cache
                    )
                )
            }
        }

        return errors
    }
}

/**
 * Validates a [LocalChildComponentConfig], checking its property bindings against the child blueprint.
 */
private fun validateLocalChild(
    path: Name,
    childName: Name,
    childConfig: LocalChildComponentConfig,
    childDeclaration: DeviceBlueprintDeclaration,
    errors: MutableList<ValidationError>,
) {
    childConfig.bindings.forEach { binding ->
        val targetSpec = childDeclaration.properties.find { it.name == binding.target.propertyName }

        // Check if target property exists, is mutable, and has a matching type name
        if (targetSpec == null) {
            errors.add(
                ValidationError.InvalidBinding(
                    path,
                    childName,
                    binding.target.propertyName,
                    "Target property not found in child blueprint '${childDeclaration.id}'."
                )
            )
            return@forEach
        }

        if (!targetSpec.mutable) {
            errors.add(
                ValidationError.InvalidBinding(
                    path,
                    childName,
                    binding.target.propertyName,
                    "Target property is not mutable."
                )
            )
        }

        // Check type compatibility declared in binding vs actual property
        TODO()

        // Check specific binding logic
        when (binding) {
            is SourcePropertyBinding -> {
                if (binding.source.valueTypeName != binding.target.valueTypeName) {
                    errors.add(ValidationError.InvalidBinding(path, childName, binding.target.propertyName, "Type mismatch: Source is '${binding.source.valueTypeName}', but target expects '${binding.target.valueTypeName}'."))
                }
            }

            is ConstPropertyBinding -> {
                // Validate meta value against descriptor
                if (!targetSpec.metaDescriptor.validate(binding.value)) {
                    errors.add(ValidationError.InvalidBinding(path, childName, binding.target.propertyName, "Constant value '${binding.value}' is not valid for target property according to its descriptor."))
                }
            }

            is TransformedPropertyBinding -> {
                if (binding.source.valueTypeName != binding.transformer.inputValueTypeName) {
                    errors.add(ValidationError.InvalidBinding(path, childName, binding.target.propertyName, "Transformer input type mismatch: Source is '${binding.source.valueTypeName}', but transformer expects '${binding.transformer.inputValueTypeName}'."))
                }
                if (binding.transformer.outputValueTypeName != binding.target.valueTypeName) {
                    errors.add(ValidationError.InvalidBinding(path, childName, binding.target.propertyName, "Transformer output type mismatch: Transformer produces '${binding.transformer.outputValueTypeName}', but target expects '${binding.target.valueTypeName}'."))
                }
            }
        }
    }
}

/**
 * Validates a [RemoteChildComponentConfig], checking if the referenced peer connection is defined in the parent.
 */
private fun validateRemoteChild(
    path: Name,
    parentDeclaration: DeviceBlueprintDeclaration,
    childName: Name,
    childConfig: RemoteChildComponentConfig,
    errors: MutableList<ValidationError>,
) {
    if (!parentDeclaration.peerConnections.containsKey(childConfig.peerName)) {
        errors.add(
            ValidationError.InvalidRemoteChild(
                path,
                childName,
                "Peer connection with name '${childConfig.peerName}' is not defined in the parent blueprint."
            )
        )
    }
}

/**
 * Performs a "shallow" validation of a [DeviceBlueprintDeclaration], checking for internal consistency
 * (naming collisions, capability requirements).
 */
private fun validateSelf(path: Name, declaration: DeviceBlueprintDeclaration): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()

    // Check for name collisions between all types of members within the same namespace
    val allNames = declaration.properties.map { it.name } +
            declaration.actions.map { it.name } +
            declaration.streams.map { it.name } +
            declaration.children.keys +
            declaration.peerConnections.keys +
            declaration.alarms.map { it.name }

    if (allNames.size != allNames.toSet().size) {
        val duplicates = allNames.groupBy { it }.filter { it.value.size > 1 }.keys
        duplicates.forEach { errors.add(ValidationError.ConflictingName(path, it)) }
    }

    // Validate consistency of declared capabilities (Features) vs. used members
    // 1. PlanExecutor
    if (declaration.actions.any { it.logic is PlanLogic }) {
        if (declaration.features.values.none { it.capability == PlanExecutorDevice.CAPABILITY }) {
            errors.add(
                ValidationError.InconsistentCapability(
                    path,
                    declaration.id.value,
                    PlanExecutorDevice.CAPABILITY
                )
            )
        }
    }

    // 2. TaskExecutor
    if (declaration.actions.any { it.logic is TaskLogic }) {
        if (declaration.features.values.none { it.capability == TaskExecutorDevice.CAPABILITY }) {
            errors.add(
                ValidationError.InconsistentCapability(
                    path,
                    declaration.id.value,
                    TaskExecutorDevice.CAPABILITY
                )
            )
        }
    }

    // 3. StatefulDevice (Persistence)
    if (declaration.properties.any { it.policies.state?.persistent == true || it.kind == PropertyKind.CONFIGURATION }) {
        if (declaration.features.values.none { it.capability == StatefulDevice.CAPABILITY }) {
            errors.add(
                ValidationError.InconsistentCapability(
                    path,
                    declaration.id.value,
                    StatefulDevice.CAPABILITY
                )
            )
        }
    }

    // 4. Alarms
    if (declaration.alarms.isNotEmpty()) {
        if (declaration.features.values.none { it is AlarmsFeature }) {
            errors.add(
                ValidationError.InconsistentCapability(
                    path,
                    declaration.id.value,
                    "space.kscience.controls.composite.model.alarms.AlarmSource"
                )
            )
        }
    }

    return errors
}
