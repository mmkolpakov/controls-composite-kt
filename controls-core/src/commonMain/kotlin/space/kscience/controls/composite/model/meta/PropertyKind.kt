package space.kscience.controls.composite.model.meta

import kotlinx.serialization.Serializable

/**
 * Semantic classification for a device property.
 * Helps UI generators and clients understand how to treat the variable and what controls to render.
 */
@Serializable
public enum class PropertyKind {
    /**
     * Read-only physical input. Corresponds to PLC Input Registers or Discrete Inputs.
     * Typically visualized as a graph or a status indicator.
     */
    SENSOR,

    /**
     * Read-write physical setting. Corresponds to PLC Holding Registers or Coils.
     * Typically visualized as an Input Box, Switch, or Slider.
     */
    SETPOINT,

    /**
     * Internal configuration parameter. Changes rarely and may require a device restart or
     * specific reconfiguration sequence. Typically visualized in a separate settings form.
     */
    CONFIGURATION,

    /**
     * Read-only derived/computed value. Calculated based on other properties.
     */
    CALCULATED,

    /**
     * Special kind for logic predicates (boolean conditions).
     * Used in guards and state machine transitions.
     */
    PREDICATE,

    /**
     * Input for an action or logic. Not a persistent state of the device.
     */
    ACTION_ARGUMENT
}
