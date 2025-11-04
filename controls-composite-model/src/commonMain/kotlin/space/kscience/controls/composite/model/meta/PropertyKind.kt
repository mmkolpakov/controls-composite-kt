package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Serializable

/**
 * A semantic classification for a device property, describing its nature and origin.
 * This enumeration allows the runtime, validation tools, and UI generators to understand
 * how a property behaves without inspecting its implementation. For example, the runtime
 * can avoid unnecessary I/O operations by not calling the driver for properties that are
 * not of the `PHYSICAL` kind.
 */
@Serializable
public enum class PropertyKind {
    /**
     * Represents a property whose value is read directly from a physical device,
     * a remote service, or a simulation via the `DeviceDriver`. Access to this
     * property typically implies an I/O operation.
     */
    PHYSICAL,

    /**
     * Represents a property that holds internal, managed state within a device.
     * Its value is not directly tied to a hardware read but is part of the device's
     * logical state. This kind is typically assigned to properties created with
     * the `stateProperty` delegate.
     */
    LOGICAL,

    /**
     * Represents a property whose value is computed from one or more other properties
     * (its dependencies). It does not hold its own state but reflects the combined
     * or transformed state of its sources.
     */
    DERIVED,

    /**
     * Represents a special kind of `DERIVED` property that always returns a `Boolean`
     * value. It represents a logical condition or a state predicate of the device
     * (e.g., `isReady`, `isInRange`, `temperatureIsNormal`). This kind allows UI generators
     * to automatically create status indicators and automation systems to use it for
     * conditional logic in plans.
     */
    PREDICATE,
}