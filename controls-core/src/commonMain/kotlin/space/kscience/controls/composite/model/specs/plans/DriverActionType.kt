package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.Serializable

/**
 * Defines the specific lifecycle action to be performed by a DeviceDriver.
 * This is used within a DriverActionSpec to make lifecycle hooks an explicit part of a plan.
 */
@Serializable
public enum class DriverActionType {
    /** Corresponds to the logic that acquires and initializes low-level resources (e.g., opens a socket). */
    CONNECT,

    /** Corresponds to the logic that releases low-level resources (e.g., closes a socket). */
    DISCONNECT
}
