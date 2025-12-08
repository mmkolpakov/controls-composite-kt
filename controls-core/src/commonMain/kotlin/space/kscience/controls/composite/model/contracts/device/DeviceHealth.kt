package space.kscience.controls.composite.model.contracts.device

import kotlinx.serialization.Serializable

/**
 * Represents the operational health status of a device.
 * This provides a standardized way for the Runtime to report connection issues,
 * hardware faults, or maintenance modes to the Hub and UI.
 */
@Serializable
public enum class HealthState {
    /**
     * The device is connected, operational, and functioning within normal parameters.
     */
    OK,

    /**
     * The device is connected and partially operational, but some features may be unavailable
     * or performance may be reduced (e.g., one sensor out of many is failed).
     */
    DEGRADED,

    /**
     * The device is explicitly placed in maintenance mode. Data may be simulated or invalid,
     * but this is an expected condition. Alerts should typically be suppressed.
     */
    MAINTENANCE,

    /**
     * The device is in an error state due to internal failure (e.g., configuration error, hardware fault).
     */
    ERROR,

    /**
     * The device is not reachable. Communication is lost.
     * This is equivalent to the "Last Will" concept in MQTT.
     */
    OFFLINE
}
