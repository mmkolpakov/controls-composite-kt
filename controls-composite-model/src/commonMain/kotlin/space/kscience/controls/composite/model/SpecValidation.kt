package space.kscience.controls.composite.model

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.DeviceBlueprint
import space.kscience.controls.composite.model.contracts.PlanExecutorDevice
import space.kscience.controls.composite.model.contracts.TaskExecutorDevice
import space.kscience.controls.composite.model.discovery.BlueprintRegistry
import space.kscience.controls.composite.model.meta.MutableDevicePropertySpec
import space.kscience.dataforge.meta.descriptors.validate
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName

/**
 * Represents a specific validation error found in a [DeviceBlueprint].
 */
public sealed interface ValidationError {
    public val message: String

    /** A validation error indicating a name collision between different types of members (e.g., a property and an action with the same name). */
    public data class ConflictingName(val name: Name) : ValidationError {
        override val message: String get() = "Name collision for '$name'. Properties, actions, children, and peer connections must have unique names."
    }

    /** A validation error indicating an issue with a property binding for a child device. */
    public data class InvalidBinding(val childName: Name, val propertyName: Name, val reason: String) :
        ValidationError {
        override val message: String get() = "Invalid binding for property '$propertyName' of child '$childName': $reason"
    }

    /** A validation error indicating that a required component, like a driver, is missing. */
    public data class MissingComponent(val componentName: String, val context: String) : ValidationError {
        override val message: String get() = "Missing required component '$componentName' in blueprint '$context'."
    }

    /**
     * A validation error indicating that the blueprint uses a capability (e.g., a plan-based action)
     * but does not declare the corresponding feature. This ensures the blueprint is self-describing.
     */
    public data class InconsistentCapability(val blueprintId: String, val capabilityName: String) : ValidationError {
        override val message: String get() = "Blueprint '$blueprintId' uses actions requiring '$capabilityName' but does not declare the corresponding feature."
    }

    /** A validation error indicating that a child's blueprint could not be found in the registry. */
    public data class BlueprintNotFound(val childName: Name, val blueprintId: BlueprintId) : ValidationError {
        override val message: String get() = "Blueprint with ID '${blueprintId}' for child '$childName' not found in the registry."
    }

    /** A validation error related to a remote child configuration. */
    public data class InvalidRemoteChild(val childName: Name, val reason: String) : ValidationError {
        override val message: String get() = "Invalid configuration for remote child '$childName': $reason"
    }

    /** A generic validation error for other issues. */
    public data class GenericError(val context: String, override val message: String) : ValidationError
}

/**
 * A utility object for validating [DeviceBlueprint] instances.
 */
public object CompositeSpecValidator {

    /**
     * Validates a given [DeviceBlueprint] for common configuration errors against a [BlueprintRegistry].
     * The registry is required to resolve and validate child blueprints.
     *
     * @param blueprint The blueprint to validate.
     * @param registry The registry used to look up child blueprint definitions.
     * @return A list of [ValidationError] found. An empty list signifies a valid blueprint.
     */
    public fun validate(blueprint: DeviceBlueprint<*>, registry: BlueprintRegistry): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        // 1. Check for name collisions
        val allNames = blueprint.properties.keys +
                blueprint.actions.keys +
                blueprint.children.keys +
                blueprint.peerConnections.keys

        if (allNames.size != allNames.toSet().size) {
            val duplicates = allNames.groupBy { it }.filter { it.value.size > 1 }.keys
            duplicates.forEach { errors.add(ValidationError.ConflictingName(it)) }
        }

        // 2. Validate child configurations
        for ((childName, childConfig) in blueprint.children) {
            when (childConfig) {
                is LocalChildComponentConfig -> validateLocalChild(blueprint, childName, childConfig, registry, errors)
                is RemoteChildComponentConfig -> validateRemoteChild(blueprint, childName, childConfig, errors)
            }
        }

        // 3. Validate consistency of declared capabilities (Features)
        validateCapabilities(blueprint, errors)

        return errors
    }

    private fun validateCapabilities(blueprint: DeviceBlueprint<*>, errors: MutableList<ValidationError>){
        // Check for plan-based actions
        val hasPlanActions = blueprint.actions.values.any { it.descriptor.meta["plan".asName()] != null }
        if (hasPlanActions) {
            val hasPlanFeature = blueprint.features.values.any { it.capability == PlanExecutorDevice.CAPABILITY }
            if(!hasPlanFeature){
                errors.add(ValidationError.InconsistentCapability(blueprint.id.value, PlanExecutorDevice.CAPABILITY))
            }
        }

        // Check for task-based actions
        val hasTaskActions = blueprint.actions.values.any { it.descriptor.taskBlueprintId != null }
        if (hasTaskActions) {
            val hasTaskFeature = blueprint.features.values.any { it.capability == TaskExecutorDevice.CAPABILITY }
            if(!hasTaskFeature){
                errors.add(ValidationError.InconsistentCapability(blueprint.id.value, TaskExecutorDevice.CAPABILITY))
            }
        }
    }

    private fun validateLocalChild(
        parentBlueprint: DeviceBlueprint<*>,
        childName: Name,
        childConfig: LocalChildComponentConfig,
        registry: BlueprintRegistry,
        errors: MutableList<ValidationError>
    ) {
        val childBlueprint = registry.findById(childConfig.blueprintId)
        if (childBlueprint == null) {
            errors.add(ValidationError.BlueprintNotFound(childName, childConfig.blueprintId))
            return // Can't validate bindings if the blueprint is missing
        }

        childConfig.bindings.bindings.forEach { binding ->
            when (binding) {
                is ParentPropertyBinding -> {
                    val sourceSpec = parentBlueprint.properties[binding.sourceName]
                    val targetSpec = childBlueprint.properties[binding.targetName]

                    if (sourceSpec == null) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Source property '${binding.sourceName}' does not exist in the parent blueprint '${parentBlueprint.id}'."
                        ))
                    }
                    if (targetSpec == null) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Target property '${binding.targetName}' does not exist in child blueprint '${childBlueprint.id}'."
                        ))
                    } else if (targetSpec !is MutableDevicePropertySpec<*, *>) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Target property '${binding.targetName}' in child blueprint '${childBlueprint.id}' is not mutable and cannot be bound to."
                        ))
                    }
                    // Check for type compatibility using stable type names from descriptors
                    if (sourceSpec != null && targetSpec != null && sourceSpec.descriptor.valueTypeName != targetSpec.descriptor.valueTypeName) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Type mismatch: Parent property '${sourceSpec.name}' has type '${sourceSpec.descriptor.valueTypeName}', " +
                                    "but child property '${targetSpec.name}' expects type '${targetSpec.descriptor.valueTypeName}'."
                        ))
                    }
                }
                is ConstPropertyBinding -> {
                    val targetSpec = childBlueprint.properties[binding.targetName]
                    if (targetSpec == null) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Target property '${binding.targetName}' not found in child blueprint '${childBlueprint.id}'."
                        ))
                    } else if (targetSpec !is MutableDevicePropertySpec<*, *>) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Target property '${binding.targetName}' in child blueprint '${childBlueprint.id}' is not mutable."
                        ))
                    } else {
                        // Validate that the constant value conforms to the target property's descriptor.
                        if (!targetSpec.descriptor.metaDescriptor.validate(binding.value)) {
                            errors.add(ValidationError.InvalidBinding(
                                childName,
                                binding.targetName,
                                "Constant value '${binding.value}' is not valid for the target property '${targetSpec.name}' according to its MetaDescriptor."
                            ))
                        }
                        // Also validate if the meta can be converted to the target type.
                        try {
                            targetSpec.converter.read(binding.value)
                        } catch (e: Exception) {
                            errors.add(ValidationError.InvalidBinding(
                                childName,
                                binding.targetName,
                                "Constant meta '${binding.value}' cannot be converted to the target type '${targetSpec.descriptor.valueTypeName}'. Reason: ${e.message}"
                            ))
                        }
                    }
                }
                is TransformedPropertyBinding -> {
                    if (parentBlueprint.properties[binding.sourceName] == null) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Source property '${binding.sourceName}' for transformed binding does not exist in parent blueprint '${parentBlueprint.id}'."
                        ))
                    }
                    if (childBlueprint.properties[binding.targetName] == null) {
                        errors.add(ValidationError.InvalidBinding(
                            childName,
                            binding.targetName,
                            "Target property '${binding.targetName}' for transformed binding does not exist in child blueprint '${childBlueprint.id}'."
                        ))
                    }
                }
            }
        }
    }

    private fun validateRemoteChild(
        parentBlueprint: DeviceBlueprint<*>,
        childName: Name,
        childConfig: RemoteChildComponentConfig,
        errors: MutableList<ValidationError>
    ) {
        if (!parentBlueprint.peerConnections.containsKey(childConfig.peerName)) {
            errors.add(ValidationError.InvalidRemoteChild(
                childName,
                "Peer connection with name '${childConfig.peerName}' is not defined in the parent blueprint '${parentBlueprint.id}'."
            ))
        }
    }
}