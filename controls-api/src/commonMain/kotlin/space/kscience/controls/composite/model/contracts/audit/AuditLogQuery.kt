package space.kscience.controls.composite.model.contracts.audit

import kotlinx.serialization.Serializable
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * A serializable specification for a query against the audit log.
 * This object can be sent over the network to a remote audit service.
 *
 * @property startTime The beginning of the time range for the query (inclusive).
 * @property endTime The end of the time range for the query (inclusive).
 * @property filterMeta A [Meta] object representing additional, serializable filter criteria.
 *                      The interpretation of this metadata is left to the `AuditLogService` implementation.
 *                      It could, for example, contain patterns for device names, message types, or correlation IDs.
 */
@Serializable
public data class AuditLogQuery(
    val startTime: Instant,
    val endTime: Instant,
    val filterMeta: Meta = Meta.EMPTY,
) {
    public companion object {
        /** A key in [filterMeta] for a regex pattern to match against the device's full hierarchical name. */
        public val DEVICE_NAME_PATTERN_KEY: Name = Name.of("filter", "deviceName")
        /** A key in [filterMeta] for the exact message type string (from `@SerialName`). */
        public val MESSAGE_TYPE_KEY: Name = Name.of("filter", "messageType")
        /** A key in [filterMeta] for the correlation ID to trace a specific operation. */
        public val CORRELATION_ID_KEY: Name = Name.of("filter", "correlationId")
    }
}
