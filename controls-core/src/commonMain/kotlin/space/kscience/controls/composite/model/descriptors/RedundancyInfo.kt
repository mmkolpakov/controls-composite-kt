package space.kscience.controls.composite.model.descriptors

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * Defines the operational role of this Hub instance within a High Availability (HA) cluster.
 */
@Serializable
public enum class RedundancyRole {
    /**
     * The Hub is the Active/Primary node. It is polling devices, executing logic,
     * and is the authoritative source of truth.
     */
    MASTER,

    /**
     * The Hub is in Standby/Backup mode. It may be synchronized with the Master
     * but is not actively controlling devices to avoid conflicts.
     */
    BACKUP,

    /**
     * The Hub is operating in standalone mode, not part of any redundancy cluster.
     */
    INDEPENDENT
}

/**
 * Defines the synchronization state of the redundancy cluster.
 */
@Serializable
public enum class RedundancyState {
    /** The node is fully synchronized with its peer. Failover is safe. */
    SYNCED,

    /** The node is currently synchronizing state (e.g., after restart). Failover may result in data loss. */
    SYNCING,

    /** Synchronization has failed or the peer is unreachable. The cluster is degraded. */
    FAILED,

    /** The redundancy state is unknown or not applicable. */
    UNKNOWN
}

/**
 * A descriptor containing runtime information about the High Availability status of the Hub.
 * This is critical for monitoring systems and UIs to display "Split Brain" warnings or
 * indicate which node is currently active.
 *
 * @property role The current role of this hub instance.
 * @property state The current synchronization state.
 * @property peerId The identifier of the peer node (if configured).
 * @property activeSince The timestamp when this node became MASTER (if applicable).
 */
@Serializable
public data class RedundancyInfo(
    val role: RedundancyRole = RedundancyRole.INDEPENDENT,
    val state: RedundancyState = RedundancyState.UNKNOWN,
    val peerId: String? = null,
    val activeSince: String? = null // ISO-8601 string or null
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
