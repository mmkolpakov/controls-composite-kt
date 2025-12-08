package space.kscience.controls.composite.model.contracts.device

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import space.kscience.controls.composite.model.contracts.logic.ExecutableDeviceBlueprint
import space.kscience.controls.composite.model.contracts.runtime.ConstructorElement
import space.kscience.controls.composite.model.contracts.runtime.ReactiveScope
import space.kscience.controls.composite.model.contracts.runtime.StatefulDelegateElement
import space.kscience.controls.composite.model.contracts.runtime.VirtualMutableDeviceState
import space.kscience.controls.composite.model.state.InitializableDevice
import space.kscience.controls.composite.model.state.StatefulDeviceLogic
import space.kscience.controls.composite.model.state.value
import space.kscience.dataforge.meta.MetaConverter
import space.kscience.dataforge.names.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A capability interface for devices that can have their state saved and restored.
 * This interface **declares** which properties are part of the persistent state but **does not**
 * implement the persistence logic itself. The actual `snapshot` and `restore` operations
 * are handled by an external `StatePersistenceService`, which operates on the properties
 * exposed via `statefulProperties`.
 *
 * A [ExecutableDeviceBlueprint] must declare this capability for it to be used.
 */
public interface StatefulDevice : Device, ReactiveScope, InitializableDevice {
    /**
     * Companion object holding stable identifiers for the capability.
     */
    public companion object {
        /**
         * The unique, fully-qualified name for the [StatefulDevice] capability.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.model.state.StatefulDevice"
    }

    /**
     * A delegate providing thread-safe logic for managing stateful properties and their persistence.
     * A concrete implementation of [StatefulDevice] is expected to provide an instance of [StatefulDeviceLogic].
     */
    public val statefulLogic: StatefulDeviceLogic

    /**
     * A reference to the blueprint from which this device was instantiated. Available after initialization.
     */
    public val blueprint: ExecutableDeviceBlueprint<out Device> get() = statefulLogic.blueprint

    /**
     * A collection of all properties managed by the `stateful` delegate, which constitute the persistent state
     * of this device. This is the single source of truth for the device's persistent state.
     */
    public val statefulProperties: Set<StatefulDelegateElement<*>> get() = statefulLogic.statefulProperties

    // Default implementations for StateContainer delegation
    override val constructorElements: Set<ConstructorElement> get() = statefulLogic.constructorElements
    override fun registerElement(constructorElement: ConstructorElement): Unit = statefulLogic.registerElement(constructorElement)
    override fun unregisterElement(constructorElement: ConstructorElement): Unit = statefulLogic.unregisterElement(constructorElement)
    override fun initialize(blueprint: ExecutableDeviceBlueprint<out Device>): Unit = statefulLogic.initialize(blueprint)

    public val isDirty: StateFlow<Boolean> get() = statefulLogic.isDirty
    public val dirtyVersion: StateFlow<Long> get() = statefulLogic.dirtyVersion
    public suspend fun markDirty(): Unit = statefulLogic.markDirty()
    public suspend fun clearDirtyFlag(expectedVersion: Long): Boolean = statefulLogic.clearDirtyFlag(expectedVersion)
}


/**
 * A delegate factory for creating and automatically registering a **stateful, persistent property**.
 * This is the **only** correct way to define a property whose state should be managed by the
 * [StatefulDevice] persistence mechanism.
 *
 * This function is an extension on `StatefulDevice`, making the dependency explicit at compile time.
 * Using this delegate ensures that the property is discoverable for automatic state saving and loading,
 * and that any changes to its value will correctly mark the device as "dirty".
 *
 * @param T The type of the property's value.
 * @param initialValue The initial value of the property.
 * @param converter A [MetaConverter] for serializing and deserializing the property's value.
 * @return A [PropertyDelegateProvider] that creates a read-write property delegate.
 */
public fun <T> StatefulDevice.stateful(
    initialValue: T,
    converter: MetaConverter<T>,
): PropertyDelegateProvider<Any?, ReadWriteProperty<Any?, T>> =
    PropertyDelegateProvider { thisRef, property ->
        require(thisRef === this) {
            "The `stateful` delegate must be used on the StatefulDevice instance itself."
        }

        val state = VirtualMutableDeviceState(initialValue)
        val element = StatefulDelegateElement(property.name, state, converter)
        registerElement(element)

        object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T =
                state.value ?: initialValue // Fallback to initial value.

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                // `device` (and its method markDirty) is available from the receiver.
                launch { markDirty() }
                launch { state.update(value) }
            }
        }
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
