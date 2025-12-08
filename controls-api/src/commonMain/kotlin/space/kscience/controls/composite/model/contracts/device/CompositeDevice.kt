package space.kscience.controls.composite.model.contracts.device

import space.kscience.controls.composite.model.specs.device.ActionDescriptor
import space.kscience.controls.composite.model.specs.device.PropertyDescriptor
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.provider.Provider

/**
 * A specialized contract for a [Device] that can contain other devices as children.
 * This interface extends the base `Device` contract with the capabilities of a DataForge [Provider],
 * allowing for hierarchical introspection of its members.
 *
 * Any device that defines children in its blueprint must implement this interface. The runtime
 * is responsible for ensuring this.
 */
public interface CompositeDevice : Device {

    /**
     * Provides content for DataForge's [Provider] mechanism. A runtime implementation of [CompositeDevice]
     * **must** override this method to expose its properties, actions, and child devices for introspection.
     *
     * Standard targets:
     * - [Device.PROPERTY_TARGET]: Exposes [PropertyDescriptor]s.
     * - [Device.ACTION_TARGET]: Exposes [ActionDescriptor]s.
     * - [CHILD_DEVICE_TARGET]: Exposes child [Device]s.
     *
     * @param target A string identifier for the type of content being requested.
     * @return A map of named content items.
     */
    override fun content(target: String): Map<Name, Any>

    /**
     * Retrieves a direct child device by its local name.
     *
     * @param name The simple (single-token) or hierarchical local name of the child device.
     * @return The [Device] instance if found, otherwise `null`.
     */
    public fun getChildDevice(name: Name): Device?

    public companion object {
        /** DataForge provider target for accessing child devices. */
        public const val CHILD_DEVICE_TARGET: String = "child"
    }
}
