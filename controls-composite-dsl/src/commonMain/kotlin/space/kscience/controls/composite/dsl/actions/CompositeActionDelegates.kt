package space.kscience.controls.composite.dsl.actions

import kotlinx.coroutines.withContext
import kotlinx.serialization.serializer
import space.kscience.controls.composite.dsl.DeviceSpecification
import space.kscience.controls.composite.dsl.properties.ActionDescriptorBuilder
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.PlanExecutorDevice
import space.kscience.controls.composite.model.contracts.TaskExecutorDevice
import space.kscience.controls.composite.model.features.PlanExecutorFeature
import space.kscience.controls.composite.model.features.TaskExecutorFeature
import space.kscience.controls.composite.model.meta.*
import space.kscience.controls.composite.model.plans.TransactionPlan
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.meta.MetaConverter.Companion.meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * A base delegate for creating a device action specification. This is the generic entry point
 * used by more specialized, type-safe action delegates like [unitAction] or [metaAction].
 *
 * This function should be used within a [space.kscience.controls.composite.dsl.DeviceSpecification] to define an action.
 * The resulting [DeviceActionSpec] is **automatically registered** in the blueprint being constructed.
 *
 * @param D The type of the device contract.
 * @param I The input type of the action.
 * @param O The output type of the action.
 * @param inputConverter A [MetaConverter] to handle serialization of the input type.
 * @param outputConverter A [MetaConverter] to handle serialization of the output type.
 * @param descriptorBuilder A DSL block to configure the action's static [ActionDescriptor], including its description,
 *                          metadata, and links to the operational FSM.
 * @param name An optional explicit name for the action. If not provided, the delegated property name is used.
 * @param execute A suspendable lambda that defines the action's runtime logic. It receives the device instance (`this`)
 *                and the action's input as parameters.
 * @return A [PropertyDelegateProvider] for a read-only property that holds the created [DeviceActionSpec].
 */
public fun <D : Device, I, O> DeviceSpecification<D>.action(
    inputConverter: MetaConverter<I>,
    outputConverter: MetaConverter<O>,
    descriptorBuilder: ActionDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    execute: suspend D.(input: I) -> O?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DeviceActionSpec<D, I, O>>> {
    return PropertyDelegateProvider { thisRef, property ->
        val actionName = name ?: property.name.parseAsName()
        val dslBuilder = ActionDescriptorBuilder(actionName).apply(descriptorBuilder)
        val descriptor = dslBuilder.build()

        val devAction = object : DeviceActionSpec<D, I, O> {
            override val name: Name = actionName
            override val descriptor: ActionDescriptor = descriptor
            override val inputConverter: MetaConverter<I> = inputConverter
            override val outputConverter: MetaConverter<O> = outputConverter
            override val operationalEventTypeName: String? get() = descriptor.operationalEventTypeName
            override val operationalSuccessEventTypeName: String? get() = descriptor.operationalSuccessEventTypeName
            override val operationalFailureEventTypeName: String? get() = descriptor.operationalFailureEventTypeName

            override suspend fun execute(device: D, input: I): O? =
                withContext(device.coroutineContext) { device.execute(input) }
        }
        // Automatic registration in the specification builder
        thisRef.registerActionSpec(devAction)
        ReadOnlyProperty { _, _ -> devAction }
    }
}


/**
 * Declares a device action that takes no input ([Unit]) and produces no output ([Unit]).
 * This is the most common type of action for simple commands.
 * To link this action to an operational FSM event, use the `triggers` function
 * inside the `descriptorBuilder` block.
 *
 * @see action
 */
public fun <D : Device> DeviceSpecification<D>.unitAction(
    descriptorBuilder: ActionDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    execute: suspend D.() -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DeviceActionSpec<D, Unit, Unit>>> = action(
    MetaConverter.unit, MetaConverter.unit, descriptorBuilder, name
) { execute() }

/**
 * Declares a device action that takes a [Meta] object as input and returns a [Meta] object as output.
 * This is useful for generic or complex actions where strong typing of inputs and outputs is not required,
 * allowing for flexible, data-driven commands.
 *
 * @see action
 */
public fun <D : Device> DeviceSpecification<D>.metaAction(
    descriptorBuilder: ActionDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    execute: suspend D.(Meta) -> Meta?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DeviceActionSpec<D, Meta, Meta>>> = action(
    meta, meta, descriptorBuilder, name, execute
)

/**
 * Declares a device action that is implemented by a `dataforge-data` `Task`.
 * This function is constrained to device contracts `D` that implement [TaskExecutorDevice], ensuring
 * compile-time safety. Using this function automatically adds [TaskExecutorFeature] to the blueprint.
 *
 * The runtime is responsible for discovering the `TaskBlueprint` by its ID and invoking
 * [TaskExecutorDevice.executeTask]. The local `execute` block of this action spec serves as a safeguard,
 * throwing an error to prevent direct execution by a non-task-aware runtime.
 *
 * @param D The device contract, which **must** implement [TaskExecutorDevice].
 * @param I The input type for the task, must be `@Serializable`.
 * @param O The output type for the task, must be `@Serializable`.
 * @param taskBlueprintId The unique identifier of the `TaskBlueprint` to be executed.
 * @param descriptorBuilder A DSL block for further configuring the action's descriptor.
 * @param name An optional explicit name for the action.
 */
@OptIn(DFExperimental::class)
public inline fun <reified I, reified O, D> DeviceSpecification<D>.taskAction(
    taskBlueprintId: String,
    crossinline descriptorBuilder: ActionDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DeviceActionSpec<D, I, O>>> where D : Device, D : TaskExecutorDevice {
    return PropertyDelegateProvider { thisRef, property ->
        // Automatically register the required feature for this capability.
        thisRef.registerFeature(TaskExecutorFeature(listOf(taskBlueprintId)))

        val actionName = name ?: property.name.parseAsName()
        val dslBuilder = ActionDescriptorBuilder(actionName).apply {
            this.taskBlueprintId = taskBlueprintId
            this.taskInputTypeName = serializer<I>().descriptor.serialName
            this.taskOutputTypeName = serializer<O>().descriptor.serialName
            descriptorBuilder()
        }
        val descriptor = dslBuilder.build()

        val devAction = object : DeviceActionSpec<D, I, O> {
            override val name: Name = actionName
            override val descriptor: ActionDescriptor = descriptor
            override val inputConverter: MetaConverter<I> = MetaConverter.serializable()
            override val outputConverter: MetaConverter<O> = MetaConverter.serializable()
            override val operationalEventTypeName: String? get() = descriptor.operationalEventTypeName
            override val operationalSuccessEventTypeName: String? get() = descriptor.operationalSuccessEventTypeName
            override val operationalFailureEventTypeName: String? get() = descriptor.operationalFailureEventTypeName

            override suspend fun execute(device: D, input: I): O {
                error(
                    "Action '$actionName' on device '${device.name}' is backed by a DataForge Task. " +
                            "It must be executed by a task-aware runtime via the 'TaskExecutorDevice.executeTask' contract, not directly."
                )
            }
        }

        thisRef.registerActionSpec(devAction)
        ReadOnlyProperty { _, _ -> devAction }
    }
}

/**
 * Declares a device action implemented by a [TransactionPlan].
 * This function is constrained to device contracts `D` that implement [PlanExecutorDevice], ensuring
 * compile-time safety. Using this function automatically adds [PlanExecutorFeature] to the blueprint.
 *
 * The plan is serialized into the action's descriptor metadata. The runtime is responsible for
 * finding a [space.kscience.controls.composite.model.services.TransactionCoordinator] and executing the plan,
 * which should ultimately invoke the [PlanExecutorDevice.executePlan] method on the device.
 * The local `execute` block throws an error to prevent direct execution by a non-plan-aware runtime.
 *
 * @param D The device contract, which **must** implement [PlanExecutorDevice].
 * @param name An optional explicit name for the plan-based action.
 * @param descriptorBuilder A DSL block for configuring the action's descriptor.
 * @param block A DSL block defining the [TransactionPlan].
 */
public fun <D> DeviceSpecification<D>.plan(
    name: Name? = null,
    descriptorBuilder: ActionDescriptorBuilder.() -> Unit = {},
    block: PlanBuilder.() -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DeviceActionSpec<D, Unit, Unit>>> where D : Device, D : PlanExecutorDevice {
    val plan = plan(block)
    val planMeta = plan.toMeta()

    return PropertyDelegateProvider { thisRef, property ->
        // Automatically register the required feature for this capability.
        thisRef.registerFeature(PlanExecutorFeature())

        val actionName = name ?: property.name.parseAsName()
        val action = unitAction(
            descriptorBuilder = {
                meta {
                    "plan" put planMeta
                }
                // Apply user-provided configuration
                descriptorBuilder()
            },
            name = actionName,
        ) {
            error(
                "Action '$actionName' on device '${this.name}' is backed by a TransactionPlan. " +
                        "It must be executed by a plan-aware runtime via the 'PlanExecutorDevice.executePlan' contract, not directly."
            )
        }
        action.provideDelegate(thisRef, property)
    }
}