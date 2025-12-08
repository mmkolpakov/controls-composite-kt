package space.kscience.controls.composite.model.messages

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * Defines rules for filtering and downsampling telemetry on the server side (Hub/Edge) before transmission.
 * This implies a "Smart Subscription" model similar to GraphQL subscriptions or SCADA Deadbands.
 *
 * Using these filters significantly reduces network bandwidth usage and client-side processing load
 * by preventing "noise" (insignificant changes) from flooding the stream.
 *
 * @property topicPattern A hierarchical pattern to match topics (e.g., "factory.line1.**" or "**.temperature").
 *                        Uses DataForge name matching rules.
 * @property deadbandAbsolute The minimum absolute change required to trigger an update.
 *                            Example: If 1.0, a value changing from 100.0 to 100.5 is ignored.
 * @property deadbandPercent The minimum relative change (in percent, 0.0-100.0) required to trigger an update.
 *                           Example: 1.0 means 1% change.
 * @property minInterval The minimum time between consecutive updates (Throttling).
 *                       Useful for high-frequency sensors where the UI only renders at 60Hz or 1Hz.
 * @property maxInterval The maximum time between updates (Heartbeat).
 *                       Ensures the client receives a value at least this often, confirming the connection
 *                       and sensor health, even if the value hasn't changed.
 */
@Serializable
public data class TelemetryFilter(
    val topicPattern: Name,
    val deadbandAbsolute: Double? = null,
    val deadbandPercent: Double? = null,
    val minInterval: Duration? = null,
    val maxInterval: Duration? = null
)
