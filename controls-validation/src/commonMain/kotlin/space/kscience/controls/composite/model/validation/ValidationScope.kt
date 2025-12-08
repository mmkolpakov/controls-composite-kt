package space.kscience.controls.composite.model.validation

import space.kscience.dataforge.names.Name

/**
 * An immutable validation context that tracks the state of the execution environment
 * at a specific point.
 *
 * @property variables The set of variables currently declared and visible in the scope.
 * @property executedActionKeys The set of action keys (idempotency keys) that have definitely
 *                             executed prior to the current step. Used to validate [ActionOutputTarget].
 */
internal data class ValidationScope(
    val variables: Set<Name> = emptySet(),
    val executedActionKeys: Set<String> = emptySet()
) {
    fun declareVariable(name: Name): ValidationScope = copy(variables = variables + name)

    fun recordActionKey(key: String?): ValidationScope =
        if (key != null) copy(executedActionKeys = executedActionKeys + key) else this

    fun hasVariable(name: Name): Boolean = name in variables
    fun hasActionKey(key: String): Boolean = key in executedActionKeys
}
