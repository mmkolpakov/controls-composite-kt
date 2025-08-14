package space.kscience.controls.composite.dsl.properties

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.serialization.serializer
import space.kscience.controls.composite.dsl.DeviceSpecification
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.runtime.CompositeDeviceContext
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.controls.composite.model.state.DeviceState
import space.kscience.controls.composite.model.state.Quality
import space.kscience.controls.composite.model.state.StateValue
import space.kscience.controls.composite.model.state.okState
import space.kscience.controls.composite.model.state.value
import space.kscience.dataforge.context.error
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.asName
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.time.Clock

/**
 * Creates a delegate for a read-only, derived property and simultaneously registers its specification.
 * The value of this property is computed reactively from one or more source properties.
 * This is an experimental, advanced feature.
 *
 * This function uses [PropertyDelegateProvider] to ensure that the [DevicePropertySpec] is
 * registered eagerly at the time of property declaration, not lazily on first access.
 *
 * @param D The type of the device contract.
 * @param T The type of the property value.
 * @param converter The [MetaConverter] for the property's value type, essential for serialization and type safety.
 * @param dependencies A vararg list of [DevicePropertySpec] that this property depends on.
 * @param initialValue An initial value for the state before the first computation.
 * @param descriptorBuilder A DSL block to configure the property's descriptor.
 * @param read A suspendable lambda that takes the current values of the dependencies and computes the derived value.
 * @return A [PropertyDelegateProvider] that registers the property and provides a [HydratableDeviceState] at build time.
 */
@DFExperimental
@OptIn(ExperimentalCoroutinesApi::class, InternalControlsApi::class)
public inline fun <D : Device, reified T : Any> DeviceSpecification<D>.derived(
    converter: MetaConverter<T>,
    vararg dependencies: DevicePropertySpec<in D, *>,
    initialValue: T,
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    noinline read: suspend D.(values: List<Any?>) -> T,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, HydratableDeviceState<D, T>>> =
    PropertyDelegateProvider { thisRef, property ->
        lateinit var propertySpec: DevicePropertySpec<D, T>

        val specProvider = thisRef.internalProperty(
            converter = converter,
            name = property.name.asName(),
            descriptorBuilder = descriptorBuilder
        ) {
            val deviceContext = (this as? CompositeDeviceContext)
                ?: error("Device must implement CompositeDeviceContext to read a derived property state.")
            deviceContext.getState(propertySpec).value
        }
        propertySpec = specProvider.provideDelegate(thisRef, property).getValue(thisRef, property)

        ReadOnlyProperty { _, _ ->
            HydratableDeviceState { device ->
                val deviceContext = (device as? CompositeDeviceContext)
                    ?: error("Device must implement CompositeDeviceContext to hydrate a derived state.")

                val flows = dependencies.map { depSpec ->
                    deviceContext.getState(depSpec).stateFlow
                }

                combine(flows) { states ->
                    val values = states.map { it.value }
                    try {
                        val computedValue = device.read(values)
                        val combinedTimestamp = states.maxOf { it.timestamp }
                        val combinedQuality =
                            if (states.all { it.quality == Quality.OK }) {
                                Quality.OK
                            } else {
                                Quality.INVALID
                            }
                        StateValue(computedValue, combinedTimestamp, combinedQuality)
                    } catch (e: Exception) {
                        device.context.logger.error(e) { "Failed to compute derived property '${propertySpec.name}'" }
                        StateValue(
                            initialValue,
                            states.maxOfOrNull { it.timestamp } ?: device.clock.now(),
                            Quality.ERROR
                        )
                    }
                }.stateIn(
                    scope = device,
                    started = SharingStarted.Lazily,
                    initialValue = okState(initialValue, device.clock)
                ).let { stateFlow ->
                    object : DeviceState<T> {
                        override val stateValue: StateValue<T?> get() = stateFlow.value
                        override val stateFlow: StateFlow<StateValue<T?>> get() = stateFlow
                    }
                }
            }
        }
    }