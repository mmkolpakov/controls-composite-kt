package space.kscience.controls.composite.old

import space.kscience.controls.core.identifiers.BlueprintId
import space.kscience.controls.composite.old.contracts.DeviceBlueprint
import space.kscience.controls.composite.old.contracts.PlanExecutorDevice
import space.kscience.controls.composite.old.contracts.TaskExecutorDevice
import space.kscience.controls.core.descriptors.PropertyKind
import space.kscience.controls.composite.old.state.StatefulDevice
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.names.Name

/**
 * A sealed interface representing a specific validation error found in a [DeviceBlueprint].
 */
public sealed interface ValidationError {
    public val message: String

    /** A validation error indicating a name collision between different types of members. */
    public data class ConflictingName(val name: Name) : ValidationError {
        override val message: String get() = "Name collision for '$name'. Properties, actions, streams, children, and peer connections must have unique names."
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

    /** A validation warning for features that are declared but not used. */
    public data class SuperfluousCapability(val blueprintId: String, val capabilityName: String) : ValidationError {
        override val message: String get() = "Blueprint '$blueprintId' declares capability '$capabilityName' via its feature, but does not appear to use any members that require it. This might be unintentional."
    }

    /** A validation error indicating that a blueprint uses a capability but does not declare the corresponding feature. */
    public data class InconsistentCapability(val blueprintId: String, val capabilityName: String) : ValidationError {
        override val message: String get() = "Blueprint '$blueprintId' uses members requiring '$capabilityName' but does not declare the corresponding feature."
    }

    /** A validation error indicating that a child's blueprint could not be found in the registry. */
    public data class BlueprintNotFound(val childName: Name, val blueprintId: BlueprintId) : ValidationError {
        override val message: String get() = "Blueprint with ID '${blueprintId}' for child '$childName' not found in the registry."
    }

    /** A validation error related to a remote child configuration. */
    public data class InvalidRemoteChild(val childName: Name, val reason: String) : ValidationError {
        override val message: String get() = "Invalid configuration for remote child '$childName': $reason"
    }

    /** A validation error related to an operational guard. */
    public data class InvalidGuard(val blueprintId: String, val predicateName: Name, val reason: String) : ValidationError {
        override val message: String get() = "Invalid guard in blueprint '$blueprintId' for predicate '$predicateName': $reason"
    }

    /** A validation error related to an action's precondition. */
    public data class InvalidActionRequirement(
        val blueprintId: String,
        val actionName: Name,
        val predicateName: Name,
        val reason: String,
    ) : ValidationError {
        override val message: String get() = "Invalid requirement for action '$actionName' in blueprint '$blueprintId': $reason"
    }

    /** A validation error related to a remote property mirror. */
    public data class InvalidMirror(val blueprintId: String, val localName: Name, val reason: String) : ValidationError {
        override val message: String get() = "Invalid mirror '$localName' in blueprint '$blueprintId': $reason"
    }

    /** A validation error for a cyclical dependency between blueprints. */
    public data class CyclicalDependency(val path: List<BlueprintId>) : ValidationError {
        override val message: String get() = "Cyclical blueprint dependency detected: ${path.joinToString(" -> ") { it.value }}"
    }

    /** A generic validation error for other issues. */
    public data class GenericError(val context: String, override val message: String) : ValidationError
}

/**
 * An exception thrown by `compositeDeviceValidated` when one or more validation errors are found.
 * @property errors A list of all validation errors found.
 */
public class BlueprintValidationException(public val errors: List<ValidationError>) :
    IllegalArgumentException("Blueprint validation failed with ${errors.size} error(s):\n${errors.joinToString("\n") { "  - ${it.message}" }}")


/**
 * Performs a "shallow" validation of a [DeviceBlueprint], checking only for internal consistency.
 *
 * @return A list of [ValidationError].
 */
public fun DeviceBlueprint<*>.validateSelf(): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()

    // Check for name collisions
    val allNames = properties.keys + actions.keys + streams.keys + children.keys + peerConnections.keys
    if (allNames.size != allNames.toSet().size) {
        val duplicates = allNames.groupBy { it }.filter { it.value.size > 1 }.keys
        duplicates.forEach { errors.add(ValidationError.ConflictingName(it)) }
    }

    // Validate consistency of declared capabilities (Features)
    errors.addAll(validateCapabilities(this))

    return errors
}

// These private functions remain here as they are part of the core validation logic.
private fun validateCapabilities(blueprint: DeviceBlueprint<*>): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()
    if (blueprint.actions.values.any { it.descriptor.meta["plan"] != null }) {
        if (blueprint.features.values.none { it.capability == PlanExecutorDevice.CAPABILITY }) {
            errors.add(ValidationError.InconsistentCapability(blueprint.id.value, PlanExecutorDevice.CAPABILITY))
        }
    }
    if (blueprint.actions.values.any { it.descriptor.taskBlueprintId != null }) {
        if (blueprint.features.values.none { it.capability == TaskExecutorDevice.CAPABILITY }) {
            errors.add(ValidationError.InconsistentCapability(blueprint.id.value, TaskExecutorDevice.CAPABILITY))
        }
    }
    if (blueprint.properties.values.any { it.descriptor.persistent || it.descriptor.kind == PropertyKind.LOGICAL }) {
        if (blueprint.features.values.none { it.capability == StatefulDevice.CAPABILITY }) {
            errors.add(ValidationError.InconsistentCapability(blueprint.id.value, StatefulDevice.CAPABILITY))
        }
    }
    return errors
}