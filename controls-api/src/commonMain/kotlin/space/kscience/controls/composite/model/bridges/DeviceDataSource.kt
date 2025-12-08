package space.kscience.controls.composite.model.bridges

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.kscience.controls.composite.model.common.ExecutionContext
import space.kscience.controls.composite.model.common.SystemPrincipal
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.hub.DeviceHub
import space.kscience.controls.composite.model.contracts.hub.ObservableDataTree
import space.kscience.controls.composite.model.contracts.hub.StructureChangeEvent
import space.kscience.controls.composite.model.messages.DeviceAttachedMessage
import space.kscience.controls.composite.model.messages.DeviceDetachedMessage
import space.kscience.controls.composite.model.messages.PropertyChangedMessage
import space.kscience.controls.composite.model.specs.device.PropertyDescriptor
import space.kscience.dataforge.data.Data
import space.kscience.dataforge.data.DataTree
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.UnsafeKType
import space.kscience.dataforge.names.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A private, unified implementation for all nodes within a Device-backed DataTree.
 * This class correctly implements the `DataTree` contract for a single device,
 * supporting both hierarchical traversal and direct access by full name.
 *
 * @param hub The root `DeviceHub`, used for all property read requests.
 * @param deviceName The local name of the device within the hub that this node belongs to.
 * @param basePath The hierarchical path from the device root to this specific node.
 * @param allSpecs The complete collection of all property specifications from the device.
 */
private class DeviceDataNode(
    private val hub: DeviceHub,
    private val deviceName: Name,
    private val basePath: Name,
    private val allSpecs: Collection<PropertyDescriptor>,
) : DataTree<Meta> {

    override val dataType: KType = typeOf<Meta>()

    /**
     * The data associated with this specific node, if it represents a terminal property.
     * The data fetching is lazy: the `readProperty` command is sent to the hub only when
     * the `Data`'s value is awaited.
     */
    @OptIn(UnsafeKType::class)
    override val data: Data<Meta>? by lazy {
        allSpecs.find { it.name == basePath }?.let { spec ->
            Data(typeOf<Meta>()) {
                // Use the public hub API to read the property.
                // The result is handled, and an exception will be thrown on failure inside await().
                hub.readProperty(deviceName, spec.name, ExecutionContext(SystemPrincipal)).getOrThrow()
            }
        }
    }

    /**
     * A map of direct child nodes. This is the core of the hierarchical structure.
     * It is constructed by finding all descendant properties and grouping them by the
     * next token in their name relative to this node's path.
     */
    override val items: Map<NameToken, DataTree<Meta>> by lazy {
        allSpecs
            .mapNotNull { spec ->
                spec.name.removeFirstOrNull(basePath)?.firstOrNull()
            }
            .toSet()
            .associateWith { token ->
                DeviceDataNode(hub, deviceName, basePath + token, allSpecs)
            }
    }

    /**
     * A flow of update notifications for properties within this branch of the tree.
     * It filters the specific device's message flow based on the `basePath` of this node.
     */
    override val updates: Flow<Name> by lazy {
        hub.devices[deviceName]?.messageFlow
            ?.map { it.message } // Extract the DeviceMessage from DeviceHubEvent
            ?.filterIsInstance<PropertyChangedMessage>()
            ?.map { it.property.asName() }
            ?.filter { it.startsWith(basePath) }
            ?.mapNotNull { it.removeFirstOrNull(basePath) } ?: emptyFlow()
    }
}

/**
 * An implementation of [ObservableDataTree] that represents the entire device hub.
 * The top-level items in this tree are the devices themselves.
 *
 * This implementation guarantees reactivity by dynamically managing subscriptions to all devices
 * in the hub, reflecting topology changes in real-time.
 */
private class DeviceHubDataTree(private val hub: DeviceHub) : ObservableDataTree<Meta> {
    override val dataType: KType = typeOf<Meta>()

    // The root of the hub does not have data itself.
    override val data: Data<Meta>? = null

    // The top-level items are the devices in the hub.
    // Since this property is accessed dynamically, it always reflects the current state of the hub's device map.
    override val items: Map<NameToken, DataTree<Meta>>
        get() = hub.devices.mapKeys { it.key.first() }.mapValues { (name, device) ->
            DeviceDataNode(
                hub = hub,
                deviceName = name.asName(),
                basePath = Name.EMPTY,
                allSpecs = device.propertyDescriptors
            )
        }

    /**
     * The `updates` flow combines property change messages from all devices in the hub.
     * It dynamically manages subscriptions to child devices using a `channelFlow`.
     */
    override val updates: Flow<Name> = channelFlow {
        val deviceJobs = mutableMapOf<Name, Job>()

        // Helper function to start listening to a specific device
        fun subscribeToDevice(name: Name, device: Device) {
            if (deviceJobs.containsKey(name)) return

            deviceJobs[name] = launch {
                device.messageFlow
                    .map { it.message }
                    .filterIsInstance<PropertyChangedMessage>()
                    .collect { message ->
                        // Construct the full path: deviceName + propertyName
                        // The tree root sees "device.property"
                        val fullPath = name + message.property.asName()
                        send(fullPath)
                    }
            }
        }

        // Helper function to stop listening
        fun unsubscribeFromDevice(name: Name) {
            deviceJobs.remove(name)?.cancel()
        }

        // 1. Initial Subscription: Subscribe to all currently existing devices
        hub.devices.forEach { (name, device) ->
            subscribeToDevice(name, device)
        }

        // 2. Dynamic Management: Listen to hub events for attachments/detachments
        launch {
            hub.events.collect { event ->
                when (val msg = event.message) {
                    is DeviceAttachedMessage -> {
                        // Attempt to retrieve the device from the Hub's map first
                        val device = hub.devices[msg.deviceName] ?: hub.findDevice(msg.sourceDevice)

                        if (device != null) {
                            subscribeToDevice(msg.deviceName, device)
                        }
                        // If device is null here, the Hub state is inconsistent or the device
                        // was detached immediately. We simply skip subscription.
                    }

                    is DeviceDetachedMessage -> {
                        unsubscribeFromDevice(msg.deviceName)
                    }

                    else -> {
                        // Ignore other messages
                    }
                }
            }
        }
    }

    /**
     * The `structureUpdates` flow listens for device attachment/detachment events from the hub's main event stream.
     */
    override val structureUpdates: Flow<StructureChangeEvent> = hub.events
        .map { it.message }
        .mapNotNull { message ->
            when (message) {
                is DeviceAttachedMessage -> StructureChangeEvent.ItemAttached(message.deviceName)
                is DeviceDetachedMessage -> StructureChangeEvent.ItemDetached(message.deviceName)
                else -> null
            }
        }
}

/**
 * Exposes a [DeviceHub] as an [ObservableDataTree<Meta>], allowing its devices and their properties
 * to be consumed as lazy, asynchronous data points within the `dataforge-workspace` ecosystem.
 *
 * The root of the returned tree represents the hub itself. Its direct children are nodes corresponding
 * to each top-level device in the hub. The tree is fully dynamic and will reflect the attachment and
 * detachment of devices via the [ObservableDataTree.structureUpdates] flow.
 *
 * @return An [ObservableDataTree] view over the hub's entire device hierarchy.
 */
public fun DeviceHub.asDataTree(): ObservableDataTree<Meta> = DeviceHubDataTree(this)
