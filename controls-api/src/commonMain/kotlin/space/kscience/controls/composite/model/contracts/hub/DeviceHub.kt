@file:OptIn(DFExperimental::class)

package space.kscience.controls.composite.model.contracts.hub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import space.kscience.controls.composite.model.alarms.AlarmAggregator
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.ExecutionContext
import space.kscience.controls.composite.model.common.SystemPrincipal
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.descriptors.ExecutionResult
import space.kscience.controls.composite.model.messages.DeviceHubEvent
import space.kscience.controls.composite.model.messages.TelemetryFilter
import space.kscience.controls.composite.model.messages.TelemetryPacket
import space.kscience.controls.composite.model.specs.faults.HubResult
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import kotlin.time.Duration

/**
 * A configuration container for attaching a single device.
 * Used in bulk operations to efficiently transmit configuration for multiple devices.
 */
public data class DeviceRequest(
    val blueprintId: BlueprintId,
    val config: Meta = Meta.EMPTY
)

/**
 * A container for a paginated list of device information.
 */
public data class DevicePage(
    val devices: Map<Name, BlueprintId>,
    val total: Int,
    val offset: Int,
    val limit: Int
)

/**
 * The primary public API for interacting with a hierarchy of composite devices.
 *
 * This interface defines high-level, transactional operations for managing the entire lifecycle of devices,
 * their data, and their security. It serves as the Control Plane entry point for the system.
 *
 * It extends [AlarmAggregator] to provide a summarized view of all alarms within the hub scope.
 */
public interface DeviceHub : AlarmAggregator {
    /**
     * An immutable map of all currently registered top-level devices, keyed by their local unique name.
     */
    public val devices: Map<Name, Device>

    /**
     * A hot flow of events occurring within this hub scope.
     * This includes messages from all attached devices (wrapped in [DeviceHubEvent]) as well as
     * hub-level lifecycle events like [space.kscience.controls.composite.model.messages.DeviceAttachedMessage].
     *
     * This flow allows building reactive clients (like [ObservableDataTree]) that update in real-time
     * as the hub's topology or device states change.
     */
    public val events: SharedFlow<DeviceHubEvent>

    /**
     * A dynamic, reactive view of all properties of all devices in this hub as a single [ObservableDataTree].
     * This provides a "DataForge-native" way to view the hub state.
     */
    public val data: ObservableDataTree<Meta>

    /**
     * Retrieves a complete map of all devices.
     * WARNING: Use with caution on large systems. Prefer [queryDevices].
     */
    public fun listDevices(): Map<Name, BlueprintId>

    /**
     * Queries devices with filtering and pagination.
     * Scalable alternative to [listDevices].
     *
     * @param filter A Meta object acting as a query filter (e.g. tags, type, location).
     * @param limit Max number of results.
     * @param offset Offset for pagination.
     * @return A [DevicePage] containing the results.
     */
    public suspend fun queryDevices(
        filter: Meta = Meta.EMPTY,
        limit: Int = 100,
        offset: Int = 0
    ): HubResult<DevicePage>

    /**
     * Finds a device by its full network-wide address.
     *
     * @param address The address of the device to find.
     * @return The device instance, or null if not found.
     */
    public fun findDevice(address: Address): Device?

    // --- Lifecycle ---

    /**
     * Attaches a new device to the hub by its blueprint ID.
     * This is a suspendable, transactional operation.
     *
     * @param name The local name for the new device.
     * @param blueprintId The ID of the blueprint to instantiate.
     * @param config Configuration metadata for the new device.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure of the transaction.
     */
    public suspend fun attach(
        name: Name,
        blueprintId: BlueprintId,
        config: Meta = Meta.EMPTY,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    /**
     * Transactionally attaches a batch of devices.
     * This operation guarantees atomicity: either all devices are attached, or none are (rollback),
     * unless the implementation explicitly supports partial batch success (documented separately).
     *
     * @param requests A map where the key is the device name and the value is its configuration request.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure of the entire batch operation.
     */
    public suspend fun attachBatch(
        requests: Map<Name, DeviceRequest>,
        context: ExecutionContext = ExecutionContext(SystemPrincipal)
    ): HubResult<Unit>

    /**
     * Detaches a device and all its children from the hub.
     *
     * @param name The name of the device to detach.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure of the transaction.
     */
    public suspend fun detach(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    /**
     * Starts a device and, if applicable, its linked children.
     * This transitions the device from STOPPED to RUNNING state.
     *
     * @param name The name of the device to start.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure of the transaction.
     */
    public suspend fun start(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    /**
     * Stops a device and, if applicable, its linked children.
     * This transitions the device to STOPPED state.
     *
     * @param name The name of the device to stop.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure of the transaction.
     */
    public suspend fun stop(
        name: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    // --- Locking Mechanism ---

    /**
     * Acquires an exclusive lock on a resource (Lease).
     *
     * @param address The address of the device owning the resource.
     * @param resource The name of the resource to lock (e.g. "motor.axis").
     * @param timeout The duration for which the lock is requested (Lease time). If the operation takes longer,
     *                the lock must be renewed, or it will expire.
     * @param context The execution context.
     * @return A [HubResult] containing a lock token string on success, or failure if locked or unavailable.
     */
    public suspend fun acquireLock(
        address: Address,
        resource: Name,
        timeout: Duration,
        context: ExecutionContext = ExecutionContext(SystemPrincipal)
    ): HubResult<String>

    /**
     * Releases a previously acquired lock.
     *
     * @param token The lock token received from [acquireLock].
     * @param context The execution context.
     * @return A [HubResult] indicating success.
     */
    public suspend fun releaseLock(
        token: String,
        context: ExecutionContext = ExecutionContext(SystemPrincipal)
    ): HubResult<Unit>

    /**
     * Administratively forces the release of a lock, regardless of who owns it.
     * This operation requires elevated permissions (e.g. "admin").
     *
     * @param address The address of the device owning the resource.
     * @param resource The name of the resource to unlock.
     * @param reason A mandatory reason for the forced unlock, which will be logged in the Audit Trail.
     * @param context The execution context containing the principal with administrative privileges.
     * @return A [HubResult] indicating success.
     */
    public suspend fun forceReleaseLock(
        address: Address,
        resource: Name,
        reason: String,
        context: ExecutionContext = ExecutionContext(SystemPrincipal)
    ): HubResult<Unit>

    // --- Operations (Control Plane) ---

    /**
     * Reads a property from a specific device using the Control Plane (Meta).
     * Use this for configuration or complex structures. For high-frequency raw data, use [subscribeTelemetry].
     *
     * @param deviceName The name of the device.
     * @param propertyName The name of the property.
     * @param context The execution context.
     * @return A [HubResult] containing the property value as [Meta] on success.
     */
    public suspend fun readProperty(
        deviceName: Name,
        propertyName: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Meta>

    /**
     * Writes a value to a property of a specific device.
     *
     * @param deviceName The name of the device.
     * @param propertyName The name of the property.
     * @param value The value to write.
     * @param priority The write priority level (1-16), following the BACnet standard.
     * @param lockToken An optional lock token.
     * @param context The execution context.
     * @return A [HubResult] indicating success or failure.
     */
    public suspend fun writeProperty(
        deviceName: Name,
        propertyName: Name,
        value: Meta,
        priority: Int = 8,
        lockToken: String? = null,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    /**
     * Executes an action on a specific device.
     *
     * @param deviceName The name of the device.
     * @param actionName The name of the action to execute.
     * @param argument Optional argument for the action.
     * @param lockToken An optional lock token.
     * @param context The execution context.
     * @return A [HubResult] containing an [ExecutionResult].
     */
    public suspend fun execute(
        deviceName: Name,
        actionName: Name,
        argument: Meta? = null,
        lockToken: String? = null,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<ExecutionResult>

    // --- Batch Operations ---

    /**
     * Reads multiple properties from one or more devices in a single operation.
     */
    public suspend fun readProperties(
        requests: Map<Address, Set<Name>>,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Map<Address, Map<Name, OperationResult<Meta>>>

    /**
     * Writes a batch of property values to one or more devices.
     */
    public suspend fun writeProperties(
        values: Map<Address, Map<Name, Meta>>,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): Map<Address, Map<Name, OperationResult<Unit>>>

    // --- Maintenance ---

    /**
     * Reconfigures a running device with new metadata.
     */
    public suspend fun reconfigure(
        name: Name,
        meta: Meta,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    /**
     * Performs a "hot swap" of a device, updating its blueprint and migrating state while keeping the name.
     */
    public suspend fun hotSwap(
        name: Name,
        newBlueprintId: BlueprintId,
        attachmentConfig: Meta = Meta.EMPTY,
        context: ExecutionContext = ExecutionContext(SystemPrincipal),
    ): HubResult<Unit>

    // --- Data Plane ---

    /**
     * Negotiates a set of aliases for a specific device.
     */
    public suspend fun negotiateAliases(
        deviceName: Name,
        context: ExecutionContext = ExecutionContext(SystemPrincipal)
    ): HubResult<Map<Name, Int>>

    /**
     * Subscribes to a high-performance telemetry stream using a smart filter.
     */
    public fun subscribeTelemetry(
        filter: TelemetryFilter
    ): Flow<TelemetryPacket>

    /**
     * Convenience overload for simple subscriptions.
     */
    public fun subscribeTelemetry(
        pattern: Name = Name.MATCH_ALL_TOKEN.asName()
    ): Flow<TelemetryPacket> = subscribeTelemetry(TelemetryFilter(pattern))
}
