package space.kscience.controls.composite.dsl.properties

import space.kscience.controls.composite.dsl.DeviceSpecification
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.misc.DFExperimental
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * Creates a read-only derived property by transforming a single source property.
 * This is a convenience DSL function on top of the generic [derived] delegate.
 *
 * @param D The type of the device contract.
 * @param T The type of the source property.
 * @param R The type of the resulting derived property.
 * @param source The source property specification.
 * @param initialValue The initial value for the derived property before the first computation.
 * @param converter A [MetaConverter] for the result type [R]. By default, it uses an experimental,
 *                  serialization-based converter.
 * @param transform A suspendable lambda to transform the value from type [T] to [R].
 * @return A [PropertyDelegateProvider] that provides a [HydratableDeviceState] at build time.
 */
@OptIn(InternalControlsApi::class, DFExperimental::class)
public inline fun <D : Device, T, reified R : Any> DeviceSpecification<D>.map(
    source: DevicePropertySpec<D, T>,
    initialValue: R,
    converter: MetaConverter<R> = MetaConverter.serializable(),
    crossinline transform: suspend (T?) -> R,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, HydratableDeviceState<D, R>>> {
    return derived(
        converter = converter,
        dependencies = arrayOf(source),
        initialValue = initialValue
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        transform(values[0] as T?)
    }
}

/**
 * Creates a read-only derived property by combining two source properties.
 * This is a convenience DSL function on top of the generic [derived] delegate.
 *
 * @param converter A [MetaConverter] for the result type [R]. By default, it uses an experimental,
 *                  serialization-based converter.
 * @param transform A suspendable lambda to combine values from two sources into the result.
 * @return A [PropertyDelegateProvider] that provides a [HydratableDeviceState] at build time.
 */
@OptIn(InternalControlsApi::class, DFExperimental::class)
public inline fun <D : Device, T1, T2, reified R : Any> DeviceSpecification<D>.combine(
    source1: DevicePropertySpec<D, T1>,
    source2: DevicePropertySpec<D, T2>,
    initialValue: R,
    converter: MetaConverter<R> = MetaConverter.serializable(),
    crossinline transform: suspend (T1?, T2?) -> R,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, HydratableDeviceState<D, R>>> {
    return derived(
        converter = converter,
        dependencies = arrayOf(source1, source2),
        initialValue = initialValue,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        transform(values[0] as T1?, values[1] as T2?)
    }
}

/**
 * Creates a read-only derived property by combining three source properties.
 *
 * @see combine
 */
@OptIn(InternalControlsApi::class, DFExperimental::class)
public inline fun <D : Device, T1, T2, T3, reified R : Any> DeviceSpecification<D>.combine(
    source1: DevicePropertySpec<D, T1>,
    source2: DevicePropertySpec<D, T2>,
    source3: DevicePropertySpec<D, T3>,
    initialValue: R,
    converter: MetaConverter<R> = MetaConverter.serializable(),
    crossinline transform: suspend (T1?, T2?, T3?) -> R,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, HydratableDeviceState<D, R>>> {
    return derived(
        converter = converter,
        dependencies = arrayOf(source1, source2, source3),
        initialValue = initialValue,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        transform(values[0] as T1?, values[1] as T2?, values[2] as T3?)
    }
}