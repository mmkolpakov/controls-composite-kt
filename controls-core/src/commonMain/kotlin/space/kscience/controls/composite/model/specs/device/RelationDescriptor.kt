package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name

/**
 * Defines the semantic type of a relationship between devices.
 * This allows the topology graph to be filtered for specific analysis (e.g., power grid analysis vs. data flow analysis).
 */
@Serializable
public enum class RelationType {
    /**
     * A physical connection involving flow of matter or energy (e.g., pipe, wire, conveyor belt).
     */
    PHYSICAL,

    /**
     * A logical dependency or control loop (e.g., "Sensor A controls Valve B").
     */
    LOGICAL,

    /**
     * A specific power supply relationship (e.g., "UPS A powers Rack B").
     */
    POWER,

    /**
     * A data transmission path (e.g., "Switch A uplinks to Router B").
     */
    DATA,

    /**
     * Spatial relationship (e.g., "Device A is contained in Room B").
     */
    SPATIAL
}

/**
 * A declarative descriptor of a relationship (edge) in the device topology graph.
 * Unlike the hierarchical parent-child structure (which is a tree), relationships form a directed graph
 * enabling Digital Twin capabilities.
 *
 * @property targetDevice The local name of the target device within the same context (hub).
 *                        For cross-hub relations, a full address string in the meta is recommended.
 * @property type The semantic type of the relationship.
 * @property role A specific role describing the relationship from the source's perspective
 *                (e.g., "feeds", "measures", "powers", "backup_for").
 * @property meta Additional attributes of the relationship (e.g., "cable_length", "pipe_diameter", "latency").
 * @property description A human-readable description of the connection.
 */
@Serializable
public data class RelationDescriptor(
    val targetDevice: Name,
    val type: RelationType,
    val role: String,
    val meta: Meta = Meta.EMPTY,
    val description: String? = null
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
