package space.kscience.controls.telemetry

import kotlinx.serialization.Serializable
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * Defines rules for filtering and downsampling telemetry on the server side (Hub/Edge) before transmission.
 */
@Serializable
public data class TelemetryFilter(
    val topicPattern: Name,
    val deadbandAbsolute: Double? = null,
    val deadbandPercent: Double? = null,
    val minInterval: Duration? = null,
    val maxInterval: Duration? = null
)