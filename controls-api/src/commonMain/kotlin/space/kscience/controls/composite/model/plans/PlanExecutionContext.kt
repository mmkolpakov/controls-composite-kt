package space.kscience.controls.composite.model.plans

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A contract for the execution context of a [space.kscience.controls.composite.model.specs.plans.TransactionPlan].
 * This interface defines the environment in which individual [ActionSpec]s are executed,
 * providing a mechanism for them to store and retrieve intermediate results, and to manage
 * lexical scope for operations like loops.
 *
 * An instance of this context is created by the [space.kscience.controls.composite.model.services.TransactionCoordinator]
 * for each plan execution and is passed internally between action handlers.
 */
public interface PlanExecutionContext {
    /**
     * Retrieves a value from the context by its unique key.
     * The value is typically a [Meta] object, representing the output of a previous action or a declared variable.
     * The lookup MUST traverse up the scope stack if the key is not found in the current scope.
     *
     * @param key The hierarchical name under which the value is stored.
     * @return The [Meta] object if found, otherwise `null`.
     */
    public suspend fun get(key: Name): Meta?

    /**
     * Modifies the value of an **existing** variable in the context.
     * The implementation MUST search for the variable up the scope stack, starting from the current scope.
     * If the variable is found, its value is updated in the scope where it was originally declared.
     * If the variable is not found in the current or any parent scope, the implementation MUST throw an exception.
     *
     * This strict behavior prevents accidental creation of variables in an unintended (e.g., global) scope.
     * To create a new variable, [declare] MUST be used.
     *
     * @param key The hierarchical name of the variable to update.
     * @param value The new [Meta] object to store.
     * @throws IllegalStateException if the variable is not found in any scope.
     */
    public suspend fun set(key: Name, value: Meta)

    /**
     * Declares and initializes a new variable **strictly in the current scope**.
     * If a variable with the same name already exists in the current scope, the implementation MUST throw an exception.
     * This is the only correct way to introduce new, locally scoped variables. It prevents accidental "shadowing"
     * of variables from outer scopes.
     *
     * @param key The hierarchical name for the new variable.
     * @param value The initial [Meta] value for the variable.
     * @throws IllegalStateException if a variable with the same name already exists in the current scope.
     */
    public suspend fun declare(key: Name, value: Meta)

    /**
     * Executes a block of code within a new, nested scope.
     * Any variables declared (`declare`) within this scope are discarded when the block completes,
     * preventing them from "leaking" into the parent scope. This is essential for implementing
     * lexically scoped constructs like the `LoopActionSpec` and `ParallelActionSpec`, where each
     * iteration or branch should have its own isolated context.
     *
     * @param block A suspendable lambda that receives the new, nested [PlanExecutionContext] as its receiver.
     * @return The result of the `block`.
     */
    public suspend fun <R> withScope(block: suspend PlanExecutionContext.() -> R): R
}
