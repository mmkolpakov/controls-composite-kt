package space.kscience.controls.composite.dsl.properties

import kotlinx.serialization.serializer
import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.composite.dsl.CompositeSpecDsl
import space.kscience.controls.composite.model.Permission
import space.kscience.controls.composite.model.meta.ActionDescriptor
import space.kscience.controls.composite.model.meta.PropertyDescriptor
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.MetaDescriptorBuilder
import space.kscience.dataforge.names.Name

/**
 * A helper DSL class for defining permissions in a structured way.
 */
@CompositeSpecDsl
public class PermissionBuilder {
    internal val permissions = mutableSetOf<Permission>()

    /**
     * A pseudo-property to assign a permission ID for read access.
     */
    public var read: String? = null
        set(value) {
            if (value != null) permissions.add(Permission(value))
            field = value
        }

    /**
     * A pseudo-property to assign a permission ID for write access.
     */
    public var write: String? = null
        set(value) {
            if (value != null) permissions.add(Permission(value))
            field = value
        }

    /**
     * A pseudo-property to assign a permission ID for execute access.
     */
    public var execute: String? = null
        set(value) {
            if (value != null) permissions.add(Permission(value))
            field = value
        }

    /**
     * Adds a custom permission by its ID.
     */
    public fun add(permissionId: String) {
        permissions.add(Permission(permissionId))
    }
}

/**
 * A mutable builder for creating [PropertyDescriptor] instances in a DSL.
 */
@CompositeSpecDsl
public class PropertyDescriptorBuilder(public val name: Name) {
    /**
     * A human-readable description for the property.
     */
    public var description: String? = null

    /**
     * A detailed help string for metrics associated with this property.
     * This will be used as the `HELP` text in Prometheus exports.
     */
    public var help: String? = null

    /**
     * A [MetaDescriptor] defining the structure, type, and constraints of the property's value.
     */
    public var metaDescriptor: MetaDescriptor = MetaDescriptor()

    /**
     * Specifies whether the property is readable. Defaults to `true`.
     */
    public var readable: Boolean = true

    /**
     * A set of permissions required to access this property.
     */
    public val permissions: MutableSet<Permission> = mutableSetOf()

    /**
     * A map of static labels for metrics associated with this property.
     */
    public val labels: MutableMap<String, String> = mutableMapOf()

    /**
     * A list of specific allowed values for this property. If set, the runtime may use this for validation.
     */
    public var allowedValues: List<Value>? = null

    /**
     * A [Meta] block for configuring metrics collection for this property (e.g., enabling counters, gauges).
     */
    public val metrics: MutableMeta = MutableMeta()

    /**
     * If true, this property's state will be included in device snapshots
     * and will be restored when the device starts if persistence is enabled.
     * Defaults to false.
     */
    public var persistent: Boolean = false

    /**
     * If true, this property will be excluded from state snapshots.
     * Defaults to false.
     */
    public var transient: Boolean = false

    /**
     * Configures the metadata descriptor for the property's value.
     * Also propagates [allowedValues] to the meta descriptor for unified validation.
     */
    public fun meta(block: MetaDescriptorBuilder.() -> Unit) {
        this.metaDescriptor = MetaDescriptor {
            block()
            // Automatically add allowedValues to the meta descriptor if they are set
            this@PropertyDescriptorBuilder.allowedValues?.let {
                this.allowedValues = it
            }
        }
    }

    /**
     * Configures metrics for this property. The runtime will use this meta
     * to set up counters, gauges, etc.
     *
     * Example:
     * ```
     * metrics {
     *     "gauge.enabled" put true
     *     "counter.reads.enabled" put true
     * }
     * ```
     */
    public fun metrics(block: MutableMeta.() -> Unit) {
        this.metrics.apply(block)
    }

    /**
     * Configures static labels for any metrics generated for this property.
     *
     * **WARNING:** Avoid using labels with high cardinality (e.g., user IDs, session IDs, request IDs).
     * Each unique combination of labels creates a new time series in Prometheus, which can lead to
     * significant memory consumption and performance degradation. Use labels for a limited,
     * well-defined set of categories.
     *
     * Example:
     * ```
     * labels {
     *     put("device_name", "motor1")
     *     put("axis", "X")
     * }
     * ```
     */
    public fun labels(block: MutableMap<String, String>.() -> Unit) {
        this.labels.apply(block)
    }


    /**
     * Adds a required permission to access this property.
     */
    public fun requires(permission: Permission) {
        permissions.add(permission)
    }

    /**
     * Adds a required permission by its string identifier.
     */
    public fun requires(permissionId: String) {
        requires(Permission(permissionId))
    }

    /**
     * Configures required permissions for accessing this property in a structured way.
     *
     * Example:
     * ```
     * permissions {
     *     read = "mydevice.property.read"
     *     write = "mydevice.property.write"
     * }
     * ```
     */
    public fun permissions(block: PermissionBuilder.() -> Unit) {
        permissions.addAll(PermissionBuilder().apply(block).permissions)
    }

    /**
     * Builds the final, immutable [PropertyDescriptor]. This method is public to be accessible from public inline functions.
     */
    public fun build(mutable: Boolean, valueTypeName: String): PropertyDescriptor = PropertyDescriptor(
        name = name,
        valueTypeName = valueTypeName,
        description = description,
        help = help,
        metaDescriptor = metaDescriptor,
        readable = readable,
        mutable = mutable,
        permissions = permissions,
        metrics = metrics.seal(),
        labels = labels.toMap(),
        persistent = persistent,
        transient = transient
    )
}

/**
 * A mutable builder for creating [ActionDescriptor] instances in a DSL.
 */
@CompositeSpecDsl
public class ActionDescriptorBuilder(public val name: Name) {
    public var description: String? = null
    public var help: String? = null
    public var meta: MutableMeta = MutableMeta()
    public var inputMetaDescriptor: MetaDescriptor = MetaDescriptor()
    public var outputMetaDescriptor: MetaDescriptor = MetaDescriptor()
    public var taskBlueprintId: String? = null
    public val permissions: MutableSet<Permission> = mutableSetOf()
    public val metrics: MutableMeta = MutableMeta()
    public val labels: MutableMap<String, String> = mutableMapOf()

    @PublishedApi
    internal var operationalEventTypeName: String? = null

    @PublishedApi
    internal var operationalEventMeta: Meta? = null

    @PublishedApi
    internal var operationalSuccessEventTypeName: String? = null

    @PublishedApi
    internal var operationalSuccessEventMeta: Meta? = null

    @PublishedApi
    internal var operationalFailureEventTypeName: String? = null

    @PublishedApi
    internal var operationalFailureEventMeta: Meta? = null

    @PublishedApi
    internal var taskInputTypeName: String? = null

    @PublishedApi
    internal var taskOutputTypeName: String? = null

    /**
     * Configures arbitrary metadata for this action. Useful for storing additional information,
     * such as a serialized transaction plan.
     */
    public fun meta(block: MutableMeta.() -> Unit) {
        this.meta.apply(block)
    }

    /**
     * Adds metadata to this action's descriptor to indicate that its invocations should be counted.
     * The runtime can use this hint to automatically create and update a counter metric.
     * @param name The name of the counter metric. Defaults to "[action_name]_invocations_total".
     */
    public fun countInvocations(name: String = "${this.name}_invocations_total") {
        metrics {
            "metric.counter.$name.enabled" put true
        }
    }

    /**
     * Adds metadata to this action's descriptor to indicate that its execution time should be measured.
     * The runtime can use this hint to automatically create and update a histogram metric.
     * @param name The name of the histogram metric. Defaults to "[action_name]_execution_duration_seconds".
     * @param buckets An optional list of upper bounds for the histogram buckets, in seconds. If not provided,
     *                a default set of buckets will be used by the runtime.
     */
    public fun measureExecutionTime(
        name: String = "${this.name}_execution_duration_seconds",
        buckets: List<Double>? = null
    ) {
        metrics {
            "metric.histogram.$name.enabled" put true
            buckets?.let {
                "metric.histogram.$name.buckets" put it.map { b -> b.asValue() }.asValue()
            }
        }
    }

    /**
     * Links this action to an event in the operational FSM. When the action is executed,
     * the specified event will be posted to the FSM *before* the action's logic runs.
     *
     * @param E The type of the event to trigger. Must be a `@Serializable` class that implements [Event].
     * @param argsBuilder A lambda to build a [Meta] object that will be used by the runtime
     *                    to construct the event instance.
     */
    public inline fun <reified E : Event> triggers(crossinline argsBuilder: MutableMeta.() -> Unit = {}) {
        this.operationalEventTypeName = serializer<E>().descriptor.serialName
        this.operationalEventMeta = MutableMeta().apply(argsBuilder).seal()
    }

    /**
     * Links this action to an event that should be posted on successful completion of the action.
     * The runtime will post this event after the action's `execute` block completes without an exception.
     *
     * @param E The type of the success event.
     * @param argsBuilder A lambda to build the [Meta] for constructing the event.
     */
    public inline fun <reified E : Event> triggersOnSuccess(crossinline argsBuilder: MutableMeta.() -> Unit = {}) {
        this.operationalSuccessEventTypeName = serializer<E>().descriptor.serialName
        this.operationalSuccessEventMeta = MutableMeta().apply(argsBuilder).seal()
    }

    /**
     * Links this action to an event that should be posted on failure of the action.
     * The runtime will post this event if the action's `execute` block throws an exception.
     *
     * @param E The type of the failure event.
     * @param argsBuilder A lambda to build the [Meta] for constructing the event.
     */
    public inline fun <reified E : Event> triggersOnFailure(crossinline argsBuilder: MutableMeta.() -> Unit = {}) {
        this.operationalFailureEventTypeName = serializer<E>().descriptor.serialName
        this.operationalFailureEventMeta = MutableMeta().apply(argsBuilder).seal()
    }

    /**
     * Configures the metadata descriptor for the action's input arguments.
     */
    public fun inputMeta(block: MetaDescriptorBuilder.() -> Unit) {
        this.inputMetaDescriptor = MetaDescriptor(block)
    }

    /**
     * Configures the metadata descriptor for the action's output/result.
     */
    public fun outputMeta(block: MetaDescriptorBuilder.() -> Unit) {
        this.outputMetaDescriptor = MetaDescriptor(block)
    }

    /**
     * Configures metrics for this action.
     * Example:
     * ```
     * metrics {
     *     "counter.invocations.enabled" put true
     *     "histogram.duration_seconds.enabled" put true
     * }
     * ```
     */
    public fun metrics(block: MutableMeta.() -> Unit) {
        this.metrics.apply(block)
    }

    /**
     * Configures static labels for any metrics generated for this action.
     * **WARNING:** See the warning about high cardinality in [PropertyDescriptorBuilder.labels].
     */
    public fun labels(block: MutableMap<String, String>.() -> Unit) {
        this.labels.apply(block)
    }


    /**
     * Adds a required permission to execute this action.
     */
    public fun requires(permission: Permission) {
        permissions.add(permission)
    }

    /**
     * Adds a required permission by its string identifier.
     */
    public fun requires(permissionId: String) {
        requires(Permission(permissionId))
    }

    /**
     * Configures required permissions for executing this action.
     */
    public fun permissions(block: PermissionBuilder.() -> Unit) {
        permissions.addAll(PermissionBuilder().apply(block).permissions)
    }

    /**
     * Builds the final, immutable [ActionDescriptor]. This method is public to be accessible from public inline functions.
     */
    public fun build(): ActionDescriptor = ActionDescriptor(
        name = name,
        description = description,
        help = help,
        meta = meta.seal(),
        inputMetaDescriptor = inputMetaDescriptor,
        outputMetaDescriptor = outputMetaDescriptor,
        taskBlueprintId = taskBlueprintId,
        permissions = permissions,
        metrics = metrics.seal(),
        labels = labels.toMap(),
        operationalEventTypeName = operationalEventTypeName,
        operationalEventMeta = operationalEventMeta,
        operationalSuccessEventTypeName = operationalSuccessEventTypeName,
        operationalSuccessEventMeta = operationalSuccessEventMeta,
        operationalFailureEventTypeName = operationalFailureEventTypeName,
        operationalFailureEventMeta = operationalFailureEventMeta,
        taskInputTypeName = taskInputTypeName,
        taskOutputTypeName = taskOutputTypeName,
    )
}