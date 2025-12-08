package space.kscience.controls.composite.model.services

import kotlinx.coroutines.flow.Flow
import space.kscience.controls.composite.model.contracts.audit.AuditLogQuery
import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A contract for a service that provides a persistent, queryable log of all [DeviceMessage]s
 * that pass through a hub. This implements the "Event Sourcing" pattern, where the log of
 * messages serves as the single source of truth for the history of the system's state.
 *
 * This service is for:
 * - **Auditing:** Providing a complete, immutable record of all actions and state changes.
 * - **Debugging:** Allowing developers to reconstruct the state of the system at any point in time ("time travel").
 * - **Analytics:** Enabling historical analysis of device behavior and system performance.
 * - **State Recovery:** Potentially allowing for the full state of a hub to be restored by replaying the event log.
 */
public interface AuditLogService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Asynchronously records a single [DeviceMessage] to the persistent log.
     * This operation should be fast and non-blocking from the caller's perspective.
     * The implementation is responsible for ensuring durability and order.
     *
     * @param message The message to record.
     */
    public suspend fun record(message: DeviceMessage)

    /**
     * Performs a query against the historical log of messages.
     *
     * @param query A [AuditLogQuery] object specifying the time range and other filter criteria.
     * @return A [Flow] of [DeviceMessage]s that match the query criteria. The flow completes
     *         when all matching historical records have been emitted.
     */
    public fun query(query: AuditLogQuery): Flow<DeviceMessage>


    public companion object : PluginFactory<AuditLogService> {
        override val tag: PluginTag = PluginTag("device.audit.log", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation (e.g., file-based, database-based)
         * must be provided by a runtime or a dedicated persistence module.
         */
        override fun build(context: Context, meta: Meta): AuditLogService {
            error("AuditLogService is a service interface and requires a runtime-specific implementation.")
        }
    }
}
