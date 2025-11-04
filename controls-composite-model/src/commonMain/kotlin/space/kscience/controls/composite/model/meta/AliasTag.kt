package space.kscience.controls.composite.model.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A standard [MemberTag] for associating a device member (property or action) with an alternative
 * name or identifier within a specific external system or protocol. This allows a single canonical
 * property in the blueprint to be mapped to different names in different contexts.
 *
 * For example, a property named `voltage` in the blueprint could have an `AliasTag("modbus", "40001")`
 * to map it to a Modbus register address, and another `AliasTag("json-api", "inputVoltage")` to map it
 * to a field in a JSON REST API.
 *
 * The runtime, protocol adapters, or importers are responsible for interpreting these tags.
 *
 * @property namespace A string that uniquely identifies the context or protocol for this alias
 *                     (e.g., "modbus", "opc-ua", "json-importer").
 * @property alias The alternative name or identifier within that namespace.
 */
@Serializable
@SerialName("tag.alias")
public data class AliasTag(
    val namespace: String,
    val alias: String,
) : MemberTag