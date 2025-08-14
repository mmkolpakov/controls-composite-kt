package space.kscience.controls.composite.dsl.actions

import space.kscience.controls.composite.dsl.children.AttachmentConfiguration
import space.kscience.controls.composite.dsl.children.deviceAttachment
import space.kscience.controls.composite.model.Address
import space.kscience.controls.composite.model.RestartStrategy
import space.kscience.controls.composite.model.contracts.DeviceBlueprint
import space.kscience.controls.composite.model.meta.MutableDevicePropertySpec
import space.kscience.controls.composite.model.plans.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import kotlin.time.DurationUnit

/**
 * A DSL marker for building device action plans.
 */
@DslMarker
public annotation class PlanDsl

/**
 * Converts an [AttachmentConfiguration] to its [Meta] representation.
 * This is an internal helper function.
 */
private fun AttachmentConfiguration.toMeta(): Meta = Meta {
    lifecycle?.let { config ->
        "lifecycle" put {
            "lifecycleMode" put config.lifecycleMode.name
            config.startTimeout?.let { "startTimeout" put it.inWholeMilliseconds }
            config.stopTimeout?.let { "stopTimeout" put it.inWholeMilliseconds }
            "onError" put config.onError.name
            "lazyAttach" put config.lazyAttach
            "restartPolicy" put {
                "maxAttempts" put config.restartPolicy.maxAttempts
                "resetOnSuccess" put config.restartPolicy.resetOnSuccess
                "strategy" put {
                    when (val strategy = config.restartPolicy.strategy) {
                        is RestartStrategy.Linear -> {
                            "type" put "linear"
                            "baseDelay" put strategy.baseDelay.toDouble(DurationUnit.MILLISECONDS)
                        }

                        is RestartStrategy.ExponentialBackoff -> {
                            "type" put "exponential"
                            "baseDelay" put strategy.baseDelay.toDouble(DurationUnit.MILLISECONDS)
                        }

                        is RestartStrategy.Fibonacci -> {
                            "type" put "fibonacci"
                            "baseDelay" put strategy.baseDelay.toDouble(DurationUnit.MILLISECONDS)
                        }
                    }
                }
            }
            "persistence" put {
                "enabled" put config.persistence.enabled
                "mode" put config.persistence.mode.name
                "interval" put config.persistence.interval.toDouble(DurationUnit.MILLISECONDS)
                "restoreOnStart" put config.persistence.restoreOnStart
            }
        }
    }
    meta?.let { "meta" put it }
    "startMode" put startMode.name
    if (childrenOverrides.isNotEmpty()) {
        "children" put {
            childrenOverrides.forEach { (name, config) ->
                set(name, config.toMeta())
            }
        }
    }
}


/**
 * A builder for creating a `Meta`-representation of a transaction plan.
 */
@PlanDsl
public class PlanBuilder {
    private val actions = mutableListOf<ActionSpec>()

    private fun buildCompensation(block: (PlanBuilder.() -> Unit)?): TransactionPlan? {
        return if (block == null) {
            null
        } else {
            plan(block)
        }
    }

    /**
     * Adds an 'attach' action to the plan.
     * @param address The address of device to attach.
     * @param blueprint The blueprint defining the device's structure and logic.
     * @param idempotencyKey An optional key for idempotency.
     * @param compensation An optional block defining a compensating plan (e.g., `detach`).
     * @param block A DSL block to configure the attachment.
     */
    public fun attach(
        address: Address,
        blueprint: DeviceBlueprint<*>,
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
        block: AttachmentConfiguration.() -> Unit = {},
    ) {
        val config = deviceAttachment(block).toMeta() // Convert DSL config to Meta
        actions.add(
            AttachActionSpec(
                address,
                blueprint.id,
                config,
                idempotencyKey,
                buildCompensation(compensation)
            )
        )
    }

    /**
     * Adds a 'detach' action to the plan.
     * @param address The address of the device to detach.
     * @param compensation An optional block defining a compensating plan (e.g., `attach`).
     */
    public fun detach(
        address: Address,
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
    ) {
        actions.add(DetachActionSpec(address, idempotencyKey, buildCompensation(compensation)))
    }

    /**
     * Creates a block of actions that will be executed sequentially.
     *
     * @param idempotencyKey An optional key for the entire sequence.
     * @param compensation An optional compensating plan for the sequence.
     * @param block The DSL block containing the sequential actions.
     */
    public fun sequence(
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
        block: PlanBuilder.() -> Unit,
    ) {
        val nestedBuilder = PlanBuilder().apply(block)
        val spec = SequenceActionSpec(
            actions = nestedBuilder.actions,
            idempotencyKey = idempotencyKey,
            compensation = buildCompensation(compensation)
        )
        actions.add(spec)
    }

    /**
     * Creates a block of actions that will be executed in parallel.
     * The runtime will wait for all actions in this block to complete before proceeding.
     */
    public fun parallel(
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
        block: PlanBuilder.() -> Unit,
    ) {
        val nestedBuilder = PlanBuilder().apply(block)
        val spec = ParallelActionSpec(
            actions = nestedBuilder.actions,
            idempotencyKey = idempotencyKey,
            compensation = buildCompensation(compensation)
        )
        actions.add(spec)
    }

    /**
     * Adds a 'start' action to the plan.
     * @param address The address of the device to start.
     * @param compensation An optional block defining a compensating plan (e.g., `stop`).
     */
    public fun start(
        address: Address,
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
    ) {
        actions.add(StartActionSpec(address, idempotencyKey, buildCompensation(compensation)))
    }

    /**
     * Adds a 'stop' action to the plan.
     * @param address The address of the device to stop.
     * @param compensation An optional block defining a compensating plan (e.g., `start`).
     */
    public fun stop(
        address: Address,
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
    ) {
        actions.add(StopActionSpec(address, idempotencyKey, buildCompensation(compensation)))
    }

    /**
     * Adds a 'write property' action to the plan.
     * @param T The type of the property value.
     * @param property The [MutableDevicePropertySpec] identifying the target property.
     * @param deviceAddress The address of the device on which to write the property.
     * @param value The value to write.
     * @param compensation An optional block defining a compensating action (e.g., writing the old value back).
     */
    public fun <T> write(
        property: MutableDevicePropertySpec<*, T>,
        deviceAddress: Address,
        value: T,
        idempotencyKey: String? = null,
        compensation: (PlanBuilder.() -> Unit)? = null,
    ) {
        val metaValue = property.converter.convert(value)
        actions.add(
            WritePropertyActionSpec(
                deviceAddress,
                property.name,
                metaValue,
                idempotencyKey,
                buildCompensation(compensation)
            )
        )
    }

    /**
     * Builds the final `TransactionPlan` object representing the plan.
     * This method defines the strategy for constructing the root action of the plan.
     *
     * - If the builder is empty (0 actions), it creates an empty `SequenceActionSpec`, representing a no-op plan.
     * - If the builder contains exactly one action, that action becomes the root of the plan.
     * - If the builder contains multiple actions, they are implicitly wrapped in a `SequenceActionSpec`
     *   to ensure they are executed in the order they were defined.
     */
    public fun build(): TransactionPlan {
        val rootAction = when (actions.size) {
            0 -> SequenceActionSpec(emptyList()) // An empty plan is a valid no-op.
            1 -> actions.first()
            else -> SequenceActionSpec(actions.toList()) // Multiple actions are treated as a sequence.
        }
        return TransactionPlan(rootAction)
    }
}

/**
 * Creates a type-safe [TransactionPlan] using a DSL.
 */
public fun plan(block: PlanBuilder.() -> Unit): TransactionPlan =
    PlanBuilder().apply(block).build()

/**
 * Creates a Meta-based representation of a transaction plan using a type-safe DSL.
 * This is useful for serialization and transport.
 */
public fun planAsMeta(block: PlanBuilder.() -> Unit): Meta =
    MetaConverter.meta.convert(plan(block).toMeta())