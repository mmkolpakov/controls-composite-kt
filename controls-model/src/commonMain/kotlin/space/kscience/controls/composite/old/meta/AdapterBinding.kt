package space.kscience.controls.composite.old.meta

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A base polymorphic interface for attaching strictly-typed, protocol-specific configuration
 * to a [PropertyDescriptor] or [ActionDescriptor]. This mechanism allows protocol adapter modules
 * (e.g., for Modbus, OPC-UA, or a custom REST API) to provide their own type-safe configuration
 * models and DSLs without modifying the core framework.
 *
 * Each implementation of this interface represents the configuration for a specific adapter.
 * The runtime, or the adapter itself, can then look up its specific binding from the descriptor's
 * `bindings` map using a unique key.
 *
 * ### Example for a Modbus Adapter:
 *
 * ```kotlin
 * @Serializable
 * @SerialName("binding.modbus")
 * data class ModbusBinding(
 *     val unitId: Int,
 *     val registerType: String,
 *     val address: Int
 * ) : AdapterBinding
 * ```
 *
 * This approach enforces type safety at compile time, provides better autocompletion in the IDE,
 * and makes device specifications more robust and readable compared to using a generic `Meta` block
 * with string keys.
 */
@Polymorphic
@Serializable
public sealed interface AdapterBinding

/**
 * A test-specific implementation of [AdapterBinding] to simulate a Modbus adapter configuration.
 */
@Serializable
@SerialName("binding.modbus.test")
public data class ModbusTestBinding(
    val unitId: Int,
    val registerAddress: Int,
    val registerType: String,
) : AdapterBinding