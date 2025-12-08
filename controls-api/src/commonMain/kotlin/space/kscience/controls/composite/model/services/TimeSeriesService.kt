package space.kscience.controls.composite.model.services

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.data.RawValue
import space.kscience.controls.composite.model.messages.TelemetryPacket
import space.kscience.controls.composite.model.state.StateValue
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * Defines how historical data should be aggregated when queried over a time range.
 */
@Serializable
public enum class AggregationType {
    /** No aggregation, return all raw data points. */
    NONE,

    /** Return the average value over the sampling interval. Valid for numeric types. */
    AVERAGE,

    /** Return the minimum value over the interval. */
    MIN,

    /** Return the maximum value over the interval. */
    MAX,

    /** Return the first value recorded in the interval. */
    FIRST,

    /** Return the last value recorded in the interval. */
    LAST,

    /** Count the number of data points in the interval. */
    COUNT
}

/**
 * Defines how data points should be interpolated when resampling or querying between known points.
 */
@Serializable
public enum class InterpolationType {
    /**
     * No interpolation. Return gaps if no data exists at the sample time.
     */
    NONE,

    /**
     * Step interpolation (Sample and Hold). The value remains constant until the next change.
     * Suitable for discrete states (e.g., On/Off, Enum).
     */
    STEP,

    /**
     * Linear interpolation. Draws a straight line between points.
     * Suitable for continuous analog values (e.g., Temperature, Pressure).
     */
    LINEAR
}

/**
 * A specialized service for storing and retrieving time-series data (telemetry).
 * Unlike [AuditLogService], which tracks actions and events, this service is optimized
 * for high-volume sensor data and plotting.
 *
 * Implementations typically back this with a Time Series Database (TSDB) like InfluxDB or VictoriaMetrics.
 */
public interface TimeSeriesService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Records a packet of telemetry data to the historian.
     * This operation is usually asynchronous and high-throughput.
     */
    public suspend fun record(packet: TelemetryPacket)

    /**
     * Reads historical data for a specific property.
     *
     * @param address The address of the device.
     * @param property The name of the property.
     * @param start The start timestamp (inclusive).
     * @param end The end timestamp (exclusive).
     * @param limit The maximum number of data points to return.
     * @param aggregation The aggregation method to apply if the data density exceeds the limit.
     * @param interpolation The interpolation strategy to use if the backend needs to align data to specific time buckets.
     * @return A flow of [StateValue]s containing the historical data.
     */
    public suspend fun readHistory(
        address: Address,
        property: Name,
        start: Instant,
        end: Instant,
        limit: Int = 1000,
        aggregation: AggregationType = AggregationType.AVERAGE,
        interpolation: InterpolationType = InterpolationType.NONE
    ): Flow<StateValue<RawValue>>

    public companion object : PluginFactory<TimeSeriesService> {
        override val tag: PluginTag = PluginTag("device.history", group = PluginTag.DATAFORGE_GROUP)

        /**
         * The default factory throws an error, as a concrete implementation must be provided by a runtime.
         */
        override fun build(context: Context, meta: Meta): TimeSeriesService {
            error("TimeSeriesService is a service interface and requires a runtime-specific implementation.")
        }
    }
}
