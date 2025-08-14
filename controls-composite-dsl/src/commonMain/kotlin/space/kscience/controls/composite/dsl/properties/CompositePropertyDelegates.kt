package space.kscience.controls.composite.dsl.properties

import kotlinx.coroutines.withContext
import kotlinx.serialization.serializer
import space.kscience.controls.composite.dsl.DeviceSpecification
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.controls.composite.model.meta.MutableDevicePropertySpec
import space.kscience.controls.composite.model.meta.PropertyDescriptor
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal fun valueDescriptor(
    valueType: ValueType,
    userBuilder: PropertyDescriptorBuilder.() -> Unit
): PropertyDescriptorBuilder.() -> Unit = {
    meta { this.valueType(valueType) }
    userBuilder()
}

// Read-only properties

@PublishedApi
internal inline fun <D : Device, reified T> DeviceSpecification<D>.internalProperty(
    converter: MetaConverter<T>,
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> T?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, T>>> =
    PropertyDelegateProvider { thisRef, property ->
        val propertyName = name ?: property.name.parseAsName()
        val dslBuilder = PropertyDescriptorBuilder(propertyName).apply(descriptorBuilder)
        val descriptor = dslBuilder.build(
            mutable = false,
            valueTypeName = serializer<T>().descriptor.serialName
        )

        val devProp = object : DevicePropertySpec<D, T> {
            override val name: Name = propertyName
            override val descriptor: PropertyDescriptor = descriptor
            override val converter: MetaConverter<T> = converter
            override val valueType: KType = typeOf<T>()

            override suspend fun read(device: D): T? = withContext(device.coroutineContext) { device.read() }
        }
        thisRef.registerPropertySpec(devProp)
        ReadOnlyProperty { _, _ -> devProp }
    }


/**
 * Creates a delegate for a read-only property specification.
 * This delegate provides a static [DevicePropertySpec] instance that is **automatically registered**
 * when the enclosing [DeviceSpecification] is configured.
 *
 * @param D The type of the device contract.
 * @param T The type of the property value.
 * @param converter The [MetaConverter] for serialization.
 * @param descriptorBuilder A DSL block to configure the property's [PropertyDescriptor].
 * @param name An optional explicit name for the property. If not provided, the delegated property name is used.
 * @param read The suspendable logic for reading the property's value from a device instance.
 */
public inline fun <D : Device, reified T> DeviceSpecification<D>.property(
    converter: MetaConverter<T>,
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> T?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, T>>> =
    internalProperty(converter, descriptorBuilder, name, read)


// Mutable properties

@PublishedApi
internal inline fun <D : Device, reified T> DeviceSpecification<D>.internalMutableProperty(
    converter: MetaConverter<T>,
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> T?,
    noinline write: suspend D.(value: T) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, T>>> =
    PropertyDelegateProvider { thisRef, property ->
        val propertyName = name ?: property.name.parseAsName()
        val dslBuilder = PropertyDescriptorBuilder(propertyName).apply(descriptorBuilder)
        val descriptor = dslBuilder.build(
            mutable = true,
            valueTypeName = serializer<T>().descriptor.serialName
        )

        val devProp = object : MutableDevicePropertySpec<D, T> {
            override val name: Name = propertyName
            override val descriptor: PropertyDescriptor = descriptor
            override val converter: MetaConverter<T> = converter
            override val valueType: KType = typeOf<T>()
            override suspend fun read(device: D): T? = withContext(device.coroutineContext) { device.read() }
            override suspend fun write(device: D, value: T): Unit =
                withContext(device.coroutineContext) { device.write(value) }
        }
        thisRef.registerPropertySpec(devProp)
        ReadOnlyProperty { _, _ -> devProp }
    }


/**
 * Creates a delegate for a mutable property specification, automatically registering it.
 *
 * @see property
 */
public inline fun <D : Device, reified T> DeviceSpecification<D>.mutableProperty(
    converter: MetaConverter<T>,
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> T?,
    noinline write: suspend D.(value: T) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, T>>> =
    internalMutableProperty(converter, descriptorBuilder, name, read, write)

// Type-safe helpers for common types, now as extensions on DeviceSpecification

/**
 * Declares a read-only property of type [Number].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.numberProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Number?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Number>>> = property(
    MetaConverter.number,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Double].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.doubleProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Double?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Double>>> = property(
    MetaConverter.double,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Float].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.floatProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Float?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Float>>> = property(
    MetaConverter.float,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Long].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.longProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Long?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Long>>> = property(
    MetaConverter.long,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Int].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.intProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Int?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Int>>> = property(
    MetaConverter.int,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [String].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.stringProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> String?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, String>>> = property(
    MetaConverter.string,
    valueDescriptor(ValueType.STRING, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Boolean].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.booleanProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Boolean?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Boolean>>> = property(
    MetaConverter.boolean,
    valueDescriptor(ValueType.BOOLEAN, descriptorBuilder),
    name,
    read
)

/**
 * Declares a read-only property of type [Meta].
 *
 * @see property
 */
public fun <D : Device> DeviceSpecification<D>.metaProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Meta?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, Meta>>> = property(
    MetaConverter.meta, descriptorBuilder, name, read
)

/**
 * Declares a read-only property of an enum type.
 *
 * @see property
 */
public inline fun <D : Device, reified E : Enum<E>> DeviceSpecification<D>.enumProperty(
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> E?,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, DevicePropertySpec<D, E>>> = property(
    MetaConverter.enum(), {
        meta { valueType(ValueType.STRING) }
        allowedValues = enumValues<E>().map { it.name.asValue() }
        descriptorBuilder()
    }, name, read
)

/**
 * Declares a mutable property of type [Boolean].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableBooleanProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Boolean?,
    write: suspend D.(Boolean) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Boolean>>> = mutableProperty(
    MetaConverter.boolean,
    valueDescriptor(ValueType.BOOLEAN, descriptorBuilder),
    name,
    read,
    write
)

/**
 * Declares a mutable property of type [Double].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableDoubleProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Double?,
    write: suspend D.(Double) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Double>>> = mutableProperty(
    MetaConverter.double,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read,
    write
)

/**
 * Declares a mutable property of type [Float].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableFloatProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Float?,
    write: suspend D.(Float) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Float>>> = mutableProperty(
    MetaConverter.float,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read,
    write
)

/**
 * Declares a mutable property of type [Long].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableLongProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Long?,
    write: suspend D.(Long) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Long>>> = mutableProperty(
    MetaConverter.long,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read,
    write
)

/**
 * Declares a mutable property of type [Int].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableIntProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Int?,
    write: suspend D.(Int) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Int>>> = mutableProperty(
    MetaConverter.int,
    valueDescriptor(ValueType.NUMBER, descriptorBuilder),
    name,
    read,
    write
)

/**
 * Declares a mutable property of type [String].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableStringProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> String?,
    write: suspend D.(String) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, String>>> = mutableProperty(
    MetaConverter.string,
    valueDescriptor(ValueType.STRING, descriptorBuilder),
    name, read, write
)

/**
 * Declares a mutable property of an enum type.
 *
 * @see mutableProperty
 */
public inline fun <D : Device, reified E : Enum<E>> DeviceSpecification<D>.mutableEnumProperty(
    noinline descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    noinline read: suspend D.() -> E?,
    noinline write: suspend D.(E) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, E>>> = mutableProperty(
    MetaConverter.enum(), {
        meta { valueType(ValueType.STRING) }
        allowedValues = enumValues<E>().map { it.name.asValue() }
        descriptorBuilder()
    }, name, read, write
)

/**
 * Declares a mutable property of type [Meta].
 *
 * @see mutableProperty
 */
public fun <D : Device> DeviceSpecification<D>.mutableMetaProperty(
    descriptorBuilder: PropertyDescriptorBuilder.() -> Unit = {},
    name: Name? = null,
    read: suspend D.() -> Meta?,
    write: suspend D.(Meta) -> Unit,
): PropertyDelegateProvider<DeviceSpecification<D>, ReadOnlyProperty<DeviceSpecification<D>, MutableDevicePropertySpec<D, Meta>>> = mutableProperty(
    MetaConverter.meta, descriptorBuilder, name, read, write
)