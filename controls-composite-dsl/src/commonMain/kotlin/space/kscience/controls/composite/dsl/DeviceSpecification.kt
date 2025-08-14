package space.kscience.controls.composite.dsl

import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.features.Feature
import space.kscience.controls.composite.model.meta.DeviceActionSpec
import space.kscience.controls.composite.model.meta.DevicePropertySpec

/**
 * A base class for creating reusable, type-safe specifications for a device blueprint.
 * This class provides an object-oriented approach to defining the structure and behavior of a device.
 *
 * ### Key Concepts
 *
 * 1.  **Reusability:** By defining a device's properties, actions, and configuration in a class,
 *     you can easily reuse this specification across different parts of your application or in different projects.
 *
 * 2.  **Automatic Registration:** Properties and actions are declared as members using delegated properties
 *     (e.g., `val myProp by doubleProperty { ... }`). These delegates automatically register their
 *     corresponding specifications (`DevicePropertySpec`, `DeviceActionSpec`) with this `DeviceSpecification` instance
 *     at the moment of its creation.
 *
 * 3.  **Structured Configuration:** The abstract `configure()` method provides a dedicated scope
 *     ([CompositeSpecBuilder]) for defining non-delegated parts of the blueprint, such as the `driver`,
 *     `children`, and `lifecycle`. This separates concerns and makes the specification easier to read.
 *
 * 4.  **Capability-Driven DSL:** Some DSL functions, like `plan` or `taskAction`, are only available if the
 *     device contract `D` implements a specific capability interface (e.g., [space.kscience.controls.composite.model.contracts.PlanExecutorDevice]).
 *     This ensures type safety at compile time. Using these functions also automatically adds the corresponding
 *     [Feature] to the blueprint, making it self-describing.
 *
 * ### How It Works
 *
 * When you use this specification with the `compositeDevice(spec, context)` factory function:
 * 1.  An instance of `CompositeSpecBuilder` is created.
 * 2.  The `apply(builder)` method is called internally.
 * 3.  All properties and actions registered via delegates are copied into the builder.
 * 4.  All features required by the used DSL functions are added to the builder.
 * 5.  The `configure()` method is called, allowing you to complete the blueprint definition.
 *
 */
public abstract class DeviceSpecification<D : Device> {
    /**
     * The unique identifier for the blueprint created from this specification.
     * If null, the simple name of the class will be used as the ID.
     * It is highly recommended to provide a unique, reverse-DNS style ID.
     */
    public open val id: String? = null

    @PublishedApi
    internal val registeredProperties: MutableList<DevicePropertySpec<D, *>> = mutableListOf()

    @PublishedApi
    internal val registeredActions: MutableList<DeviceActionSpec<D, *, *>> = mutableListOf()

    @PublishedApi
    internal val requiredFeatures: MutableMap<String, Feature> = mutableMapOf()

    @PublishedApi
    internal fun <T : DevicePropertySpec<D, *>> registerPropertySpec(spec: T): T {
        // Проверяем, что спека с таким именем еще не добавлена, чтобы избежать дублей
        if (registeredProperties.none { it.name == spec.name }) {
            registeredProperties.add(spec)
        }
        return spec
    }

    @PublishedApi
    internal fun <T : DeviceActionSpec<D, *, *>> registerActionSpec(spec: T): T {
        if (registeredActions.none { it.name == spec.name }) {
            registeredActions.add(spec)
        }
        return spec
    }

    @PublishedApi
    internal fun registerFeature(feature: Feature) {
        // Use capability name as key to avoid duplicate features
        requiredFeatures.getOrPut(feature.capability) { feature }
    }

    /**
     * Configures the device blueprint using the provided DSL builder [CompositeSpecBuilder].
     * This is where all non-delegated components (driver, children, lifecycle, metadata) should be defined.
     * All delegated properties, actions, and their required features are automatically registered.
     */
    protected abstract fun CompositeSpecBuilder<D>.configure()

    /**
     * Internal method to apply the complete configuration to a builder instance.
     * It first registers all delegated properties, actions, and required features, then calls the user-defined [configure] block.
     */
    internal fun apply(builder: CompositeSpecBuilder<D>) {
        registeredProperties.forEach { builder.registerProperty(it) }
        registeredActions.forEach { builder.registerAction(it) }
        requiredFeatures.values.forEach { builder.feature(it) }
        builder.configure()
    }
}