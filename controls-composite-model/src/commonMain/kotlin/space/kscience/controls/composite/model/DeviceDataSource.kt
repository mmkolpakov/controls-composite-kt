package space.kscience.controls.composite.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.messages.PropertyChangedMessage
import space.kscience.controls.composite.model.meta.PropertyDescriptor
import space.kscience.dataforge.data.Data
import space.kscience.dataforge.data.DataTree
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.UnsafeKType
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.asName
import space.kscience.dataforge.names.first
import space.kscience.dataforge.names.firstOrNull
import space.kscience.dataforge.names.length
import space.kscience.dataforge.names.plus
import space.kscience.dataforge.names.removeFirstOrNull
import space.kscience.dataforge.names.startsWith
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A leaf node in a [DataTree] that represents a single, non-hierarchical device property.
 * It provides a lazy [Data] object that reads the property on-demand.
 */
private class PropertyDataTreeLeaf(
    private val device: Device,
    private val propertyName: Name,
) : DataTree<Meta> {
    override val dataType: KType = typeOf<Meta>()

    @OptIn(UnsafeKType::class, InternalControlsApi::class)
    override val data: Data<Meta> by lazy {
        Data(typeOf<Meta>()) {
            device.readProperty(propertyName)
        }
    }

    override val items: Map<NameToken, DataTree<Meta>> get() = emptyMap()

    // Updates are handled by the parent adapter to avoid multiple subscriptions.
    override val updates: Flow<Name> get() = emptyFlow()
}

/**
 * An internal class representing a branch in the device's property tree.
 * A branch is created when multiple properties share a common name prefix (e.g., `motor.position`).
 * This branch then contains further sub-branches or leaves for the rest of the property names.
 *
 * This class is internal to the `asDataTree` adapter implementation.
 *
 * @param device The device instance from which to read properties.
 * @param propertyDescriptors The collection of property descriptors that fall under this branch.
 * @param basePath The common [Name] prefix for all properties in this branch.
 */
private class DeviceDataTreeBranch(
    private val device: Device,
    private val propertyDescriptors: Collection<PropertyDescriptor>,
    private val basePath: Name
) : DataTree<Meta> {
    override val dataType: KType = typeOf<Meta>()
    override val data: Data<Meta>? = null

    override val items: Map<NameToken, DataTree<Meta>> by lazy {
        val items = mutableMapOf<NameToken, MutableList<Pair<Name, PropertyDescriptor>>>()
        propertyDescriptors.forEach {
            val relativeName = it.name.removeFirstOrNull(basePath)!!
            items.getOrPut(relativeName.first()) { mutableListOf() }.add(relativeName to it)
        }

        items.mapValues { (token, list) ->
            if (list.size == 1 && list.first().first.length == 1) {
                PropertyDataTreeLeaf(device, list.first().second.name)
            } else {
                DeviceDataTreeBranch(device, list.map { it.second }, basePath + token)
            }
        }
    }

    override val updates: Flow<Name> by lazy {
        device.messageFlow
            .filterIsInstance<PropertyChangedMessage>()
            .map { it.property.asName() }
            .filter { it.startsWith(basePath) }
            .mapNotNull { it.removeFirstOrNull(basePath) }
    }
}

/**
 * An adapter that presents a [Device] as a [DataTree<Meta>].
 * The properties of the device are exposed as child nodes in the tree.
 * The root of this tree does not contain data itself, only its children do.
 */
private class DeviceAsDataTree(private val device: Device) : DataTree<Meta> {
    override val dataType: KType = typeOf<Meta>()

    /**
     * The root of a device data tree does not have its own value, it's a container for properties.
     */
    override val data: Data<Meta>? = null

    /**
     * The properties of the device are mapped to child nodes of this [DataTree].
     * Each child is a leaf node that provides the lazy-readable property value.
     */
    override val items: Map<NameToken, DataTree<Meta>> by lazy {
        val items = mutableMapOf<NameToken, MutableList<PropertyDescriptor>>()
        device.propertyDescriptors.forEach {
            items.getOrPut(it.name.first()) { mutableListOf() }.add(it)
        }

        items.mapValues { (token, list) ->
            if (list.size == 1 && list.first().name.length == 1) {
                PropertyDataTreeLeaf(device, list.first().name)
            } else {
                DeviceDataTreeBranch(device, list, token.asName())
            }
        }
    }

    /**
     * A flow of names for properties that have been updated.
     */
    override val updates: Flow<Name> by lazy {
        device.messageFlow
            .filterIsInstance<PropertyChangedMessage>()
            .map { it.property.asName() }
    }
}

/**
 * Exposes a [Device] as a [DataTree<Meta>], allowing its properties to be consumed
 * as lazy, asynchronous data points. This is the primary mechanism for integrating
 * `controls-composite` devices with `dataforge-workspace`.
 *
 * @return A [DataTree] view over the device's properties.
 */
public fun Device.asDataTree(): DataTree<Meta> = DeviceAsDataTree(this)