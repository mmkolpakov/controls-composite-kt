package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name

/**
 * A serializable, declarative mapping from a user-friendly alias to a specific path
 * within an action's output Meta. This allows for creating stable, simple shortcuts
 * to nested data in a task's result, decoupling consumers from the internal report structure.
 *
 * For example, an alias "statusCode" could map to the path "report.details.response.statusCode".
 *
 * @property alias The simple, public-facing name for the output field.
 * @property path The full DataForge [Name] path to the value within the action's result structure.
 *                An empty name (`Name.EMPTY`) is a special value that refers to the root `.data` element
 *                of the result (the primary output).
 */
@Serializable
public data class OutputAlias(
    val alias: String,
    val path: Name,
)
