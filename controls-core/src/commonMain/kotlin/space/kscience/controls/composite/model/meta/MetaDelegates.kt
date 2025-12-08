package space.kscience.controls.composite.model.meta

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.serialization.coreJson
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.serialization.instant
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.dataforge.meta.descriptors.ValueRestriction
import space.kscience.dataforge.meta.descriptors.copy
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.getIndexedList
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A property delegate for a map of serializable objects stored under a common meta node.
 * This delegate allows handling collections of complex objects in a [Scheme] in a type-safe manner.
 *
 * @param T The type of the objects in the map. The type must be `@Serializable`.
 * @param serializer The explicit [KSerializer] for type `T`. This is necessary for generic types.
 * @param key An optional explicit name for the parent meta node. Defaults to the property name.
 */
public fun <T> Scheme.mapOfConvertable(
    serializer: KSerializer<T>,
    key: Name? = null,
): MutableMetaDelegate<Map<Name, T>> = object : MutableMetaDelegate<Map<Name, T>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Map<Name, T> {
        val parentNode = meta[key ?: property.name.asName()]
        return parentNode?.items?.mapNotNull { (token, itemMeta) ->
            try {
                token.asName() to coreJson.decodeFromJsonElement(serializer, itemMeta.toJson())
            } catch (e: Exception) {
                throw IllegalArgumentException(
                    "Failed to deserialize item '${token}' in map property. " +
                            "Expected structure matching serializer '${serializer.descriptor.serialName}'.",
                    e
                )
            }
        }?.toMap() ?: emptyMap()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Map<Name, T>) {
        val parentName = key ?: property.name.asName()
        meta[parentName] = null // Clear existing items
        val parentNode = meta.getOrCreate(parentName)
        value.forEach { (childName, childValue) ->
            val childMeta = coreJson.encodeToJsonElement(serializer, childValue).toMeta()
            parentNode[childName] = childMeta
        }
    }

    override val descriptor: MetaDescriptor? by lazy {
        val itemDescriptor = MetaConverter.serializable(serializer).descriptor

        MetaDescriptor {
            multiple = true
            valueRestriction = ValueRestriction.ABSENT

            itemDescriptor?.let {
                attributes {
                    "@itemDescriptor" put it.toMeta()
                }
            }
        }
    }
}

/**
 * A property delegate for a set of serializable objects stored as same-name-siblings under a common meta node.
 * This delegate leverages `kotlinx.serialization` for conversion to and from `Meta`. It ensures uniqueness of elements
 * by using a `Set`.
 *
 * @param T The type of the objects in the set. The type must be `@Serializable`.
 * @param serializer The explicit [KSerializer] for type `T`, crucial for handling generic types.
 * @param key An optional explicit name for the property. Defaults to the delegated property name.
 * @return A [MutableMetaDelegate] for a `Set<T>`.
 */
public fun <T> Scheme.setOfConvertable(
    serializer: KSerializer<T>,
    key: Name? = null,
): MutableMetaDelegate<Set<T>> = object : MutableMetaDelegate<Set<T>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Set<T> {
        val name = key ?: property.name.asName()
        return meta.getIndexedList(name).map { itemMeta ->
            coreJson.decodeFromJsonElement(serializer, itemMeta.toJson())
        }.toSet()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<T>) {
        val name = key ?: property.name.asName()
        val metaList = value.map {
            coreJson.encodeToJsonElement(serializer, it).toMeta()
        }
        meta.setIndexed(name, metaList)
    }

    override val descriptor: MetaDescriptor? by lazy {
        val itemDescriptor = MetaConverter.serializable(serializer).descriptor
        MetaDescriptor {
            multiple = true
            valueRestriction = ValueRestriction.ABSENT
            itemDescriptor?.let {
                attributes { "@itemDescriptor" put it.toMeta() }
            }
        }
    }
}

/**
 * A property delegate for a list of serializable objects stored as same-name-siblings under a common meta node.
 * This delegate leverages `kotlinx.serialization` for conversion to and from `Meta`.
 *
 * @param T The type of the objects in the list. The type must be `@Serializable`.
 * @param serializer The explicit [KSerializer] for type `T`, crucial for handling generic types.
 * @param key An optional explicit name for the property. Defaults to the delegated property name.
 * @return A [MutableMetaDelegate] for a `List<T>`.
 */
public fun <T> Scheme.listOfConvertable(
    serializer: KSerializer<T>,
    key: Name? = null,
): MutableMetaDelegate<List<T>> = object : MutableMetaDelegate<List<T>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        val name = key ?: property.name.asName()
        return meta.getIndexedList(name).map { itemMeta ->
            coreJson.decodeFromJsonElement(serializer, itemMeta.toJson())
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) {
        val name = key ?: property.name.asName()
        val metaList = value.map {
            coreJson.encodeToJsonElement(serializer, it).toMeta()
        }
        meta.setIndexed(name, metaList)
    }

    override val descriptor: MetaDescriptor? by lazy {
        val itemDescriptor = MetaConverter.serializable(serializer).descriptor
        MetaDescriptor {
            multiple = true
            valueRestriction = ValueRestriction.ABSENT
            itemDescriptor?.let {
                attributes { "@itemDescriptor" put it.toMeta() }
            }
        }
    }
}

private fun MetaDescriptor.toMeta(): Meta {
    val json = coreJson.encodeToJsonElement(MetaDescriptor.serializer(), this)
    return json.toMeta()
}

/**
 * A convenience delegate for a set of serializable objects, stored as same-name-siblings.
 * Infers the KSerializer for the element type [T] automatically.
 *
 * @see setOfConvertable
 */
public inline fun <reified T> Scheme.setOfSerializable(
    key: Name? = null,
): MutableMetaDelegate<Set<T>> = setOfConvertable(serializer<T>(), key)

/**
 * A property delegate for a **required**, non-nullable value that is converted from Meta.
 * This is the primary declarative mechanism for defining mandatory fields in a [Scheme].
 * It throws an `IllegalStateException` if the underlying meta node is missing during a read operation.
 *
 * @param T The non-nullable type of the property's value.
 * @param converter The [MetaConverter] for serializing and deserializing the property's value.
 * @param key An optional explicit name for the property. Defaults to the delegated property name.
 * @return A [MutableMetaDelegate] for a non-nullable `T`.
 */
public fun <T : Any> Scheme.requiredConvertable(
    converter: MetaConverter<T>,
    key: Name? = null,
): MutableMetaDelegate<T> = object : MutableMetaDelegate<T> {
    override val descriptor: MetaDescriptor? get() = converter.descriptor?.copy {
        valueRestriction = ValueRestriction.REQUIRED
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val name = key ?: property.name.asName()
        val metaItem = this@requiredConvertable.meta[name]
            ?: error("Required property '${name}' is missing in the configuration.")
        return converter.read(metaItem)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val name = key ?: property.name.asName()
        this@requiredConvertable.meta[name] = converter.convert(value)
    }
}

/**
 * A convenience delegate for a required [String] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredString(key: Name? = null): MutableMetaDelegate<String> =
    requiredConvertable(MetaConverter.string, key)

/**
 * A convenience delegate for a required [Name] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredName(key: Name? = null): MutableMetaDelegate<Name> =
    requiredConvertable(MetaConverter.serializable(Name.serializer()), key)

/**
 * A convenience delegate for a required [Meta] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredMeta(key: Name? = null): MutableMetaDelegate<Meta> =
    requiredConvertable(MetaConverter.meta, key)


/**
 * A convenience delegate for a required [space.kscience.controls.composite.model.common.Address] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredAddress(key: Name? = null): MutableMetaDelegate<Address> =
    requiredConvertable(MetaConverter.serializable(Address.serializer()), key)

/**
 * A convenience delegate for a required [Boolean] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredBoolean(key: Name? = null): MutableMetaDelegate<Boolean> =
    requiredConvertable(MetaConverter.boolean, key)

/**
 * A convenience delegate for a required [Number] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredNumber(key: Name? = null): MutableMetaDelegate<Number> =
    requiredConvertable(MetaConverter.number, key)

/**
 * A convenience delegate for a required [Int] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredInt(key: Name? = null): MutableMetaDelegate<Int> =
    requiredConvertable(MetaConverter.int, key)

/**
 * A convenience delegate for a required [Double] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredDouble(key: Name? = null): MutableMetaDelegate<Double> =
    requiredConvertable(MetaConverter.double, key)

/**
 * A convenience delegate for a required [Instant] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredInstant(key: Name? = null): MutableMetaDelegate<Instant> =
    requiredConvertable(MetaConverter.instant, key)

/**
 * A convenience delegate for a required [kotlin.time.Duration] property.
 * @see requiredConvertable
 */
public fun Scheme.requiredDuration(key: Name? = null): MutableMetaDelegate<Duration> =
    requiredConvertable(MetaConverter.duration, key)


/**
 * A delegate for a required, non-nullable, serializable property.
 * Infers the KSerializer for type [T] automatically.
 * Throws an error if the value is missing.
 *
 * This is a type-safe and concise alternative to `requiredConvertable(MetaConverter.serializable(serializer<T>()))`.
 *
 * @see requiredConvertable
 * @see MetaConverter.serializable
 */
public inline fun <reified T : Any> Scheme.requiredSerializable(
    key: Name? = null,
): MutableMetaDelegate<T> = requiredConvertable(MetaConverter.serializable(serializer<T>()), key)

/**
 * A delegate for an optional, nullable, serializable property.
 * Infers the KSerializer for type [T] automatically.
 *
 * @see convertable
 * @see MetaConverter.serializable
 */
public inline fun <reified T> Scheme.serializable(
    key: Name? = null,
): MutableMetaDelegate<T?> = convertable(MetaConverter.serializable(serializer<T>()), key)

/**
 * A delegate for a list of serializable objects, stored as same-name-siblings.
 * Infers the KSerializer for the element type [T] automatically.
 *
 * @see listOfConvertable
 */
public inline fun <reified T> Scheme.listOfSerializable(
    key: Name? = null,
): MutableMetaDelegate<List<T>> = listOfConvertable(serializer<T>(), key)
