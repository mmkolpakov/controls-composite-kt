package space.kscience.controls.composite.model.contracts.logic

import space.kscience.controls.composite.model.common.ExecutionContext
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.planning.FsmContext
import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.misc.Named
import space.kscience.dataforge.names.Name

/**
 * A contract for a reusable, standalone, and discoverable piece of business logic for a device action.
 * This interface embodies the "Strategy" design pattern, allowing the logic of an action to be decoupled
 * from its declaration in a blueprint.
 *
 * Implementations of this interface can be versioned, independently tested, and reused across multiple
 * device blueprints, promoting modularity and reducing code duplication.
 *
 * @param D The type of the device on which this logic can operate. Contravariant (`in`).
 * @param I The input type for the action logic.
 * @param O The output type of the action logic.
 * @param S A [Scheme] subclass that declaratively defines the structure of the action's dependencies,
 *          providing type-safe access to them within the [execute] method.
 */
public interface DeviceActionLogic<in D : Device, in I, out O, S : Scheme> : Named {
    /**
     * The version of this logic implementation, preferably using semantic versioning.
     * This is crucial for the runtime to select a compatible implementation based on the
     * constraints defined in an [ActionDescriptor].
     */
    public val version: String

    /**
     * The specification for the dependencies [Scheme]. The runtime uses this [SchemeSpec]
     * to create a type-safe dependency object from the fetched property values before
     * passing it to the [execute] method.
     */
    public val dependencySpec: SchemeSpec<S>


    /**
     * Declares the dependencies required by this action *before* its execution.
     * The runtime will call this method first, resolve the dependencies (e.g., by reading properties),
     * and then pass them to the [execute] method in a type-safe object.
     *
     * @param input The input that will be passed to the `execute` method.
     * @return A list of [Name]s of properties that need to be read before execution.
     */
    public fun dependencies(input: I): List<Name>

    /**
     * Executes the action's business logic with type-safe dependencies and FSM interaction capabilities.
     *
     * @param device The device instance on which to execute the logic.
     * @param input The input argument for the action.
     * @param dependencies A type-safe, fully resolved object containing all declared dependencies.
     * @param fsm A context providing safe access to the device's operational FSM, allowing the logic
     *            to post events and react to the device's internal state.
     * @param context The [ExecutionContext] providing security and tracing information.
     * @return The result of the execution, or `null` if the action produces no result.
     */
    public suspend fun execute(
        device: D,
        input: I,
        dependencies: S,
        fsm: FsmContext,
        context: ExecutionContext,
    ): O?
}
