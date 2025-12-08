package space.kscience.controls.composite.model.services

import kotlinx.coroutines.flow.Flow
import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.controls.composite.model.specs.state.HubActualStateDescriptor
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import kotlin.time.Instant

/**
 * A service enabling "Time Travel" debugging and state recovery.
 * Unlike [AuditLogService] (which tracks security/business events) or [TimeSeriesService] (which tracks high-frequency sensor data),
 * this service focuses on the **System State Configuration**.
 *
 * It implements the "Event Sourcing" and "Log Compaction" patterns, allowing operators to inspect
 * exactly how the system was configured and operating at any point in the past.
 */
public interface StateLogService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Retrieves a full snapshot of the system state at a specific point in time.
     * This effectively reconstructs the [HubActualStateDescriptor] by replaying events
     * or querying a temporal database.
     *
     * @param time The point in time for the snapshot.
     * @return The reconstructed state descriptor.
     */
    public suspend fun getSnapshotAt(time: Instant): HubActualStateDescriptor

    /**
     * Replays the stream of state-changing events (Configuration, Lifecycle, Alerts)
     * that occurred within the specified time window.
     *
     * @param from The start time (inclusive).
     * @param to The end time (exclusive).
     * @return A flow of [DeviceMessage]s representing the sequence of events.
     */
    public fun replayEvents(from: Instant, to: Instant): Flow<DeviceMessage>

    public companion object : PluginFactory<StateLogService> {
        override val tag: PluginTag = PluginTag("device.state.log", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): StateLogService {
            error("StateLogService is a service interface and requires a runtime-specific implementation.")
        }
    }
}
