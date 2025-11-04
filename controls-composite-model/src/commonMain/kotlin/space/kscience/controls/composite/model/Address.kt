package space.kscience.controls.composite.model

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus

/**
 * Represents a unique, network-wide address for a device.
 * It combines a unique identifier for the hub (node) where the device resides
 * and the device's local, potentially hierarchical, name within that hub.
 *
 * ### Format Specification
 *
 * The string representation of an address follows the format: `hubId::deviceName`.
 *
 * - **`hubId`**: A string identifying the hub instance.
 *   - It MUST NOT contain the sequence `::`.
 *   - It is RECOMMENDED to use characters that are safe for file systems and network protocols (e.g., `[a-zA-Z0-9_-]`).
 *   - The comparison of `hubId` is **case-sensitive**.
 *
 * - **`::`**: A fixed, two-character separator.
 *
 * - **`deviceName`**: The local name of the device, represented by a DataForge [Name].
 *   - It follows the parsing rules of `dataforge-meta`, allowing for dot-separated tokens and optional indices in square brackets (e.g., `motor.axis[X].position`).
 *   - See `space.kscience.dataforge.names.Name` documentation for details on escaping and parsing.
 *
 * **Example:** `myLab-control-hub::motors.drive[Z].speed`
 *
 * @property hubId A unique string identifying the hub instance in the distributed system.
 * @property deviceName The local, potentially hierarchical, name of the device within its hub.
 */
@Serializable
public data class Address(val hubId: String, val deviceName: Name) {
    override fun toString(): String = "$hubId::$deviceName"

    /**
     * Creates a new [Address] for a direct child of the device at this address.
     * This is the idiomatic way to construct addresses for nested components within plans and logic.
     *
     * Example:
     * ```
     * val parentAddress = Address("myHub", "robot".asName())
     * val armAddress = parentAddress.resolveChild("arm".asName()) // -> myHub::robot.arm
     * ```
     *
     * @param childName The local name of the child device.
     * @return The full network-wide address of the child.
     */
    public fun resolveChild(childName: Name): Address =
        this.copy(deviceName = this.deviceName + childName)

    public companion object {
        /**
         * Parses a string representation of an address back into an [Address] object.
         * The expected format is "hubId::deviceName".
         *
         * @throws IllegalArgumentException if the string format is invalid.
         * @see parseOrNull for a non-throwing alternative.
         */
        public fun parse(string: String): Address {
            val parts = string.split("::", limit = 2)
            require(parts.size == 2) { "Invalid address format. Expected 'hubId::deviceName', but got '$string'." }
            return Address(parts[0], Name.parse(parts[1]))
        }

        /**
         * Parses a string representation of an address back into an [Address] object, returning null on failure.
         * This is an idiomatic, non-throwing alternative to [parse].
         *
         * @return The parsed [Address] or `null` if the string format is invalid.
         */
        public fun parseOrNull(string: String): Address? {
            val parts = string.split("::", limit = 2)
            return if (parts.size == 2) {
                Address(parts[0], Name.parse(parts[1]))
            } else {
                null
            }
        }
    }
}

/**
 * Extension function to parse a string into an [Address].
 * This provides a more fluent API for string-to-address conversion.
 *
 * @receiver The string to parse, expected format is "hubId::deviceName".
 * @throws IllegalArgumentException if the string format is invalid.
 * @see Address.parse
 */
public fun String.toAddress(): Address = Address.parse(this)

/**
 * Extension function to safely parse a string into an [Address], returning null on failure.
 * This is the recommended, idiomatic way to handle potentially invalid address strings.
 *
 * @receiver The string to parse.
 * @return The parsed [Address] or `null` if the string format is invalid.
 * @see Address.parseOrNull
 */
public fun String.toAddressOrNull(): Address? = Address.parseOrNull(this)