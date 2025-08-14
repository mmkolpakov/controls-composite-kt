package space.kscience.controls.composite.dsl

import space.kscience.controls.composite.model.contracts.Device

/**
 * A reusable fragment of a device specification. This `fun interface` allows defining a
 * collection of properties, actions, or other configurations that can be applied to multiple
 * [DeviceSpecification]s or [CompositeSpecBuilder]s, promoting code reuse and modularity.
 *
 * The type parameter `D` is invariant. This ensures strict type safety: a fragment for `MotionDevice`
 * can only be applied to builders or specifications for `MotionDevice` or its subtypes.
 *
 * ### Best Practices for Usage
 *
 * It is recommended to define fragments that operate on the [CompositeSpecBuilder]. This provides the most
 * flexibility, as these fragments can be used within both `DeviceSpecification.configure` blocks and
 * `deviceBlueprint { ... }` DSL blocks.
 *
 * **Example of a builder-based fragment:**
 * ```kotlin
 * // Define a reusable fragment for motion control using the builder context.
 * val MotionControlFragment = SpecificationFragment<MotionDevice> {
 *     // Use DSL extensions on CompositeSpecBuilder
 *     doubleProperty("position") { ... }
 *     unitAction("moveTo") { ... }
 * }
 *
 * // Apply the fragment in a specific device specification
 * object MyMotorSpec : DeviceSpecification<MyMotor>() {
 *     override fun CompositeSpecBuilder<MyMotor>.configure() {
 *         driver { ... }
 *         include(MotionControlFragment) // Re-uses all properties and actions
 *     }
 * }
 *
 * // Or apply it in a direct blueprint definition
 * val myOtherMotor = deviceBlueprint<MyMotor>("my.other.motor", context) {
 *      driver { ... }
 *      include(MotionControlFragment)
 * }
 * ```
 *
 * @param D The device contract type that this fragment applies to.
 */
public fun interface SpecificationFragment<D : Device> {
    /**
     * Applies the fragment's configuration to the given [CompositeSpecBuilder].
     */
    public fun apply(spec: CompositeSpecBuilder<D>)
}

/**
 * A DSL method within `CompositeSpecBuilder` to include a [SpecificationFragment].
 */
public fun <D : Device> CompositeSpecBuilder<D>.include(fragment: SpecificationFragment<D>) {
    fragment.apply(this)
}