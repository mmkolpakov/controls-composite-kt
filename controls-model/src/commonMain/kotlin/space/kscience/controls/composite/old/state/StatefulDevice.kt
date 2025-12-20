package space.kscience.controls.composite.old.state

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import space.kscience.controls.core.InternalControlsApi
import space.kscience.controls.core.contracts.Device
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A serializable container for a device's state snapshot, coupling the state data
 * with a version number for optimistic locking and a schema version for migration.
 *
 * @property version A monotonically increasing version number of the state, used for optimistic locking.
 * @property schemaVersion An integer representing the version of the state's schema. This is intended to be
 *                         used by a `DeviceMigrator` to correctly handle state transformations when
 *                         upgrading a device from an older blueprint version.
 * @property state A [Meta] object representing the device's state.
 */
@Serializable
public data class StateSnapshot(
    val version: Long,
    val schemaVersion: Int = 1,
    val state: Meta,
)

/**
 * A concrete implementation of the logic required for a stateful device.
 * This class manages the dirty flag and state versioning using atomic operations,
 * making it safe for concurrent access. It is intended to be used as a delegate
 * within a `StatefulDevice` implementation.
 */
public class StatefulDeviceLogic {
    private val stateCache = atomic(emptyMap<String, MutableDeviceState<*>>())

    /**
     * A thread-safe, idempotent way to get or create a state delegate.
     * This method is public to be accessible from the `stateful` delegate in another module.
     */
    public fun <T> getOrPutState(
        key: String,
        provider: () -> MutableDeviceState<T>,
    ): MutableDeviceState<T> {
        // Fast path: check if the value already exists without entering the update block.
        stateCache.value[key]?.let {
            @Suppress("UNCHECKED_CAST")
            return it as MutableDeviceState<T>
        }
        // Slow path: atomically update the map if the key is missing.
        val updatedMap = stateCache.updateAndGet { currentMap ->
            if (currentMap.containsKey(key)) {
                currentMap
            } else {
                currentMap + (key to provider())
            }
        }
        @Suppress("UNCHECKED_CAST")
        return updatedMap.getValue(key) as MutableDeviceState<T>
    }


    private val _isDirty = MutableStateFlow(false)
    public val isDirty: StateFlow<Boolean> get() = _isDirty.asStateFlow()

    private val _dirtyVersion = atomic(0L)
    private val _dirtyVersionFlow = MutableStateFlow(0L)
    public val dirtyVersion: StateFlow<Long> get() = _dirtyVersionFlow.asStateFlow()

    /**
     * Manually marks the device state as dirty by incrementing the version.
     */
    public fun markDirty() {
        _isDirty.value = true
        _dirtyVersion.incrementAndGet()
        _dirtyVersionFlow.value = _dirtyVersion.value
    }

    /**
     * Clears the dirty flag only if the current state version matches the expected version.
     * @return `true` if the flag was cleared, `false` otherwise.
     */
    @InternalControlsApi
    public fun clearDirtyFlag(expectedVersion: Long): Boolean {
        // Atomic compare-and-set operation
        val updated = _dirtyVersion.compareAndSet(expectedVersion, expectedVersion)
        if (updated) {
            _isDirty.value = false
        }
        return updated
    }
}


/**
 * A capability interface for devices that can have their state saved and restored.
 * This is used by persistence mechanisms to manage device state across restarts.
 * A [space.kscience.controls.core.contracts.DeviceBlueprint] must declare this capability for it to be used.
 */
public interface StatefulDevice : Device {
    /**
     * Companion object holding stable identifiers for the capability.
     */
    public companion object {
        /**
         * The unique, fully-qualified name for the [StatefulDevice] capability.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.old.state.StatefulDevice"
    }

    /**
     * A delegate providing thread-safe logic for managing stateful properties.
     */
    public val statefulLogic: StatefulDeviceLogic

    /**
     * A flow that indicates whether the device's state has changed since the last save.
     * `true` means the state is "dirty" and should be persisted.
     * The runtime uses this flag to optimize periodic saving.
     */
    public val isDirty: StateFlow<Boolean> get() = statefulLogic.isDirty

    /**
     * A monotonically increasing version of the device's state. Incremented by [markDirty].
     */
    public val dirtyVersion: StateFlow<Long> get() = statefulLogic.dirtyVersion

    /**
     * Manually marks the device state as dirty by incrementing the [dirtyVersion].
     * This is typically called internally when a stateful property changes.
     */
    public suspend fun markDirty() {
        statefulLogic.markDirty()
    }

    /**
     * Creates a snapshot of the device's current logical state, including its version.
     * This method should capture all serializable properties and critical state information.
     *
     * @return A [StateSnapshot] object representing the device's state.
     */
    public suspend fun snapshot(): StateSnapshot

    /**
     * Clears the dirty flag only if the current state version matches the expected version.
     * This provides a Check-and-Set (CAS) semantic to prevent race conditions where the state
     * changes between snapshot creation and persistence completion.
     *
     * This method is intended to be called by the persistence layer after a successful save.
     *
     * @return `true` if the flag was cleared, `false` otherwise (meaning the state has changed again).
     */
    @OptIn(InternalControlsApi::class)
    public suspend fun clearDirtyFlag(expectedVersion: Long): Boolean = statefulLogic.clearDirtyFlag(expectedVersion)

    /**
     * Restores the device's state from a given snapshot.
     * This is typically called on a stopped device before it is started.
     * The implementation should check the `schemaVersion` for compatibility.
     *
     * @param snapshot The [StateSnapshot] object containing the state to restore.
     */
    public suspend fun restore(snapshot: StateSnapshot)
}

/**
 * An extension of [StatefulDevice] for devices that need to persist large binary data
 * (or "blobs"), such as internal database files or configuration artifacts, which are not
 * well-suited for storage within a `Meta` object.
 *
 * The runtime persistence layer should check if a device implements this interface.
 * If it does, the runtime will call `snapshotBlobs` in addition to `snapshot` and
 * pass the results to a `SnapshotStore` that can handle binary data.
 */
public interface StatefulDeviceWithBlobs : StatefulDevice {
    /**
     * Creates a snapshot of the device's binary state artifacts.
     * This method is called by the persistence layer alongside [snapshot] when saving state.
     *
     * The key of the map is a logical name for the binary artifact (e.g., "database", "firmware_cache"),
     * and the value is its raw byte content.
     *
     * @return A map of named binary data blobs, or `null` if there are no blobs to save.
     */
    public suspend fun snapshotBlobs(): Map<Name, ByteArray>? = null

    /**
     * Restores the device's state from binary artifacts.
     * This method is called by the persistence layer alongside [restore] when loading state.
     *
     * @param blobs A map of named binary data blobs read from the snapshot store.
     */
    public suspend fun restoreBlobs(blobs: Map<Name, ByteArray>) {}
}