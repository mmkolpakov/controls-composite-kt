package space.kscience.controls.composite.model.validation

import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.dataforge.names.Name

/**
 * A sealed interface representing a specific validation error found in a [ExecutableDeviceBlueprint].
 */
public sealed interface ValidationError {
    /**
     * The hierarchical path to the element where the error occurred (e.g., "device.child[arm].property[speed]").
     */
    public val path: Name
    public val message: String

    /** A validation error indicating a name collision between different types of members. */
    public data class ConflictingName(override val path: Name, val name: Name) : ValidationError {
        override val message: String get() = "Error at $path: Name collision for '$name'. Properties, actions, streams, children, and peer connections must have unique names."
    }

    /** A validation error indicating an issue with a property binding for a child device. */
    public data class InvalidBinding(
        override val path: Name,
        val childName: Name,
        val propertyName: Name,
        val reason: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Invalid binding for property '$propertyName' of child '$childName': $reason"
    }

    /** A validation error indicating that a required component, like a driver, is missing. */
    public data class MissingComponent(
        override val path: Name,
        val componentName: String,
        val context: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Missing required component '$componentName' in blueprint '$context'."
    }

    /** A validation warning for features that are declared but not used. */
    public data class SuperfluousCapability(
        override val path: Name,
        val blueprintId: String,
        val capabilityName: String
    ) : ValidationError {
        override val message: String get() = "Warning at $path: Blueprint '$blueprintId' declares capability '$capabilityName' via its feature, but does not appear to use any members that require it."
    }

    /** A validation error indicating that a blueprint uses a capability but does not declare the corresponding feature. */
    public data class InconsistentCapability(
        override val path: Name,
        val blueprintId: String,
        val capabilityName: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Blueprint '$blueprintId' uses members requiring '$capabilityName' but does not declare the corresponding feature."
    }

    /** A validation error indicating that a child's blueprint could not be found in the registry. */
    public data class BlueprintNotFound(
        override val path: Name,
        val childName: Name,
        val blueprintId: BlueprintId
    ) : ValidationError {
        override val message: String get() = "Error at $path: Blueprint with ID '${blueprintId}' for child '$childName' not found in the registry."
    }

    /** A validation error related to a remote child configuration. */
    public data class InvalidRemoteChild(
        override val path: Name,
        val childName: Name,
        val reason: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Invalid configuration for remote child '$childName': $reason"
    }

    /** A validation error related to an operational guard. */
    public data class InvalidGuard(
        override val path: Name,
        val blueprintId: String,
        val predicateName: Name,
        val reason: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Invalid guard in blueprint '$blueprintId' for predicate '$predicateName': $reason"
    }

    /** A validation error related to an action's precondition. */
    public data class InvalidActionRequirement(
        override val path: Name,
        val blueprintId: String,
        val actionName: Name,
        val predicateName: Name,
        val reason: String,
    ) : ValidationError {
        override val message: String get() = "Error at $path: Invalid requirement for action '$actionName' in blueprint '$blueprintId': $reason"
    }

    /** A validation error related to a remote property mirror. */
    public data class InvalidMirror(
        override val path: Name,
        val blueprintId: String,
        val localName: Name,
        val reason: String
    ) : ValidationError {
        override val message: String get() = "Error at $path: Invalid mirror '$localName' in blueprint '$blueprintId': $reason"
    }

    /** A validation error for a cyclical dependency between blueprints. */
    public data class CyclicalDependency(
        override val path: Name,
        val cyclePath: List<BlueprintId>
    ) : ValidationError {
        override val message: String get() = "Error at $path: Cyclical blueprint dependency detected: ${cyclePath.joinToString(" -> ") { it.value }}"
    }

    /** A generic validation error for other issues. */
    public data class GenericError(
        override val path: Name,
        val context: String,
        override val message: String
    ) : ValidationError
}

/**
 * An exception thrown by `compositeDeviceValidated` when one or more validation errors are found.
 * @property errors A list of all validation errors found.
 */
public class BlueprintValidationException(public val errors: List<ValidationError>) :
    IllegalArgumentException("Blueprint validation failed with ${errors.size} error(s):\n${errors.joinToString("\n") { "  - ${it.message}" }}")
