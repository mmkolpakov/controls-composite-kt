@file:OptIn(InternalControlsApi::class)

package space.kscience.controls.composite.model.state

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.InternalControlsApi
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.logic.ExecutableDeviceBlueprint
import space.kscience.controls.composite.model.contracts.runtime.ConstructorElement
import space.kscience.controls.composite.model.contracts.runtime.StatefulDelegateElement
import space.kscience.dataforge.meta.Meta

/**
 * An internal contract for a device that requires post-construction initialization by the runtime.
 * This is a private bridge allowing the runtime to inject blueprint information into the device instance
 * without polluting the public `Device` API.
 */
public interface InitializableDevice {
    /**
     * Called by the runtime exactly once after the device instance is created to provide it
     * with its fully hydrated blueprint.
     *
     * @param blueprint The executable blueprint from which this device was instantiated.
     */
    public fun initialize(blueprint: ExecutableDeviceBlueprint<out Device>)
}

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
 * This class manages the dirty flag, state versioning, and the registry of stateful properties
 * using atomic operations, making it safe for concurrent access. It is intended to be used as a
 * delegate within a `StatefulDevice` implementation.
 */
public class StatefulDeviceLogic : InitializableDevice {
    private val _constructorElements = atomic(emptySet<ConstructorElement>())
    public val constructorElements: Set<ConstructorElement> get() = _constructorElements.value

    private val _blueprint = atomic<ExecutableDeviceBlueprint<*>?>(null)

    /**
     * The blueprint from which this device was created. Throws an exception if accessed before
     * the device is initialized by the runtime.
     */
    public val blueprint: ExecutableDeviceBlueprint<*>
        get() = _blueprint.value ?: error("Device has not been initialized with its blueprint by the runtime.")

    override fun initialize(blueprint: ExecutableDeviceBlueprint<out Device>) {
        if (!_blueprint.compareAndSet(null, blueprint)) {
            error("Device is already initialized. The 'initialize' method must be called only once.")
        }
    }

    /**
     * A thread-safe, lazily-initialized view of only the `StatefulDelegateElement`s,
     * used for automated persistence.
     */
    public val statefulProperties: Set<StatefulDelegateElement<*>> by lazy {
        constructorElements.filterIsInstance<StatefulDelegateElement<*>>().toSet()
    }

    public fun registerElement(element: ConstructorElement) {
        _constructorElements.update { it + element }
    }

    public fun unregisterElement(element: ConstructorElement) {
        _constructorElements.update { it - element }
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
        val newVersion = _dirtyVersion.incrementAndGet()
        _dirtyVersionFlow.value = newVersion
    }

    /**
     * Clears the dirty flag only if the current state version matches the expected version.
     * @return `true` if the flag was cleared, `false` otherwise.
     */
    public fun clearDirtyFlag(expectedVersion: Long): Boolean {
        // Atomic compare-and-set operation
        val updated = _dirtyVersion.compareAndSet(expectedVersion, expectedVersion)
        if (updated) {
            _isDirty.value = false
        }
        return updated
    }
}
