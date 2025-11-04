package space.kscience.controls.composite.model.contracts

import space.kscience.controls.composite.model.Address
import space.kscience.controls.composite.model.DeviceMigrator
import space.kscience.controls.composite.model.ExecutionContext
import space.kscience.controls.composite.model.SystemPrincipal
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * The primary public API for interacting with a hierarchy of composite devices.
 *
 * This interface defines high-level, transactional operations for managing the entire lifecycle of devices.
 * It is designed to be the main entry point for user code and external systems (like a UI or a remote client).
 * All operations are suspendable and ensure atomicity where applicable.
 */
public interface CompositeDeviceHub {
    /**
     * An immutable map of all currently registered top-level devices, keyed by their local unique name.
     * To access child devices, one must first retrieve the parent device.
     */
    public val devices: Map<Name, Device>

    /**
     * A dynamic, reactive view of all properties of all devices in this hub as a single [ObservableDataTree].
     * This provides a unified entry point for data analysis and monitoring tools that work with the
     * `dataforge-data` model. The structure of this tree mirrors the device hierarchy, and it emits
     * updates as device properties and the device topology itself change.
     */
    public val data: ObservableDataTree<Meta>

    /**
     * Retrieves a map of all devices in the hub, including nested children, and their corresponding blueprint IDs.
     * This method is essential for remote clients to discover the full topology and capabilities of the hub.
     *
     * @return A map where the key is the full hierarchical [Name] of a device and the value is its [BlueprintId].
     */
    public fun listDevices(): Map<Name, BlueprintId>

    /**
     * Finds a device by its full network-wide address. The implementation is responsible
     * for checking if the `hubId` matches its own and then resolving the `deviceName`
     * within its local device tree.
     *
     * @param address The network-wide address of the device to find.
     * @return The [Device] instance if found, otherwise `null`.
     */
    public fun findDevice(address: Address): Device?

    /**
     * Attaches a new device to the hub. This is a suspendable operation that completes
     * only when the device and its non-lazy, linked children are fully attached and in the `Stopped` state.
     *
     * @param name The unique name for the new device.
     * @param blueprint The blueprint defining the device's structure and logic.
     * @param config The attachment configuration, including lifecycle and metadata overrides, represented as a [Meta].
     * @param context The [ExecutionContext] for this operation, providing security and tracing information.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if the attachment process fails.
     */
    public suspend fun attach(
        name: Name,
        blueprint: DeviceBlueprint<*>,
        config: Meta = Meta.EMPTY,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Detaches a device and all its children from the hub. This suspendable operation
     * completes when the device is fully stopped and removed.
     *
     * @param name The name of the device to detach.
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if the detachment process fails.
     */
    public suspend fun detach(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Starts a device and, if applicable, its linked children. This suspendable operation
     * completes when the device reaches the `Running` state.
     *
     * @param name The name of the device to start.
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if the startup process fails.
     */
    public suspend fun start(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Stops a device and, if applicable, its linked children. This suspendable operation
     * completes when the device reaches the `Stopped` state.
     *
     * @param name The name of the device to stop.
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if the stopping process fails.
     */
    public suspend fun stop(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Reads a property from a specific device within the hub.
     *
     * @param deviceName The name of the target device.
     * @param propertyName The name of the property to read.
     * @param context The [ExecutionContext] for this operation.
     * @return The property value as a [Meta] object.
     * @throws space.kscience.controls.composite.model.DeviceNotFoundInCompositeHubException if the device does not exist.
     * @throws space.kscience.controls.composite.model.DevicePropertyException if the read operation fails.
     */
    public suspend fun readProperty(
        deviceName: Name,
        propertyName: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Meta

    /**
     * Writes a value to a property of a specific device within the hub.
     *
     * @param deviceName The name of the target device.
     * @param propertyName The name of the property to write.
     * @param value The [Meta] value to write.
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.DeviceNotFoundInCompositeHubException if the device does not exist.
     * @throws space.kscience.controls.composite.model.DevicePropertyException if the write operation fails.
     */
    public suspend fun writeProperty(
        deviceName: Name,
        propertyName: Name,
        value: Meta,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Executes an action on a specific device within the hub.
     *
     * @param deviceName The name of the target device.
     * @param actionName The name of the action to execute.
     * @param argument An optional [Meta] argument for the action.
     * @param context The [ExecutionContext] for this operation.
     * @return An optional [Meta] result from the action, or `null` if the action does not return a value.
     * @throws space.kscience.controls.composite.model.DeviceNotFoundInCompositeHubException if the device does not exist.
     * @throws space.kscience.controls.composite.model.DeviceActionException if the action execution fails.
     */
    public suspend fun execute(
        deviceName: Name,
        actionName: Name,
        argument: Meta? = null,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Meta?

    /**
     * Reconfigures a running device with new metadata. The device must implement [ReconfigurableDevice].
     *
     * @param name The name of the device to reconfigure.
     * @param meta The new configuration metadata to apply.
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if reconfiguration fails.
     */
    public suspend fun reconfigure(
        name: Name,
        meta: Meta,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )

    /**
     * Performs a "hot swap" of a device, replacing it with a new instance from a potentially different blueprint
     * while preserving its running state and optionally migrating its internal state.
     *
     * @param D The type of the new device.
     * @param name The name of the device to be replaced.
     * @param newBlueprint The blueprint for the new device version.
     * @param migrator An optional [DeviceMigrator] to transfer state from the old instance to the new one.
     * @param attachmentConfig The configuration for attaching the new device, represented as a [Meta].
     * @param context The [ExecutionContext] for this operation.
     * @throws space.kscience.controls.composite.model.CompositeHubTransactionException if the hot swap fails at any stage.
     */
    public suspend fun <D : Device> hotSwap(
        name: Name,
        newBlueprint: DeviceBlueprint<D>,
        migrator: DeviceMigrator<D, Meta>? = null,
        attachmentConfig: Meta = Meta.EMPTY,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    )
}