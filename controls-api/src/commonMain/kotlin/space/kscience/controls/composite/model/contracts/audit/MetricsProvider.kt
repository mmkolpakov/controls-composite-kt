package space.kscience.controls.composite.model.contracts.audit

import space.kscience.controls.composite.model.data.RawValue
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A simplified representation of a single metric value, compatible with Prometheus/OpenMetrics.
 *
 * @property name The name of the metric (e.g., "device.messages.sent").
 * @property value The current value (typically numeric).
 * @property tags Key-value pairs for dimensionality (e.g., "device_id"="motor1", "status"="error").
 */
public data class MetricValue(
    val name: Name,
    val value: RawValue,
    val tags: Map<String, String> = emptyMap()
)

/**
 * A contract for components that expose internal operational metrics using a **Pull Model**.
 * This enables integration with observability systems like Prometheus or VictoriaMetrics.
 *
 * Unlike telemetry (which monitors the physical process), these metrics monitor the *health and performance of the control system itself*
 * (e.g., queue depth, message rate, memory usage, error counts).
 */
public interface MetricsProvider : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Scrapes the current values of all metrics managed by this provider.
     * This method is typically called periodically by an exporter agent.
     *
     * @return A list of current metric values.
     */
    public fun scrape(): List<MetricValue>

    public companion object : PluginFactory<MetricsProvider> {
        override val tag: PluginTag = PluginTag("device.metrics", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): MetricsProvider {
            error("MetricsProvider is a service interface and requires a runtime-specific implementation.")
        }
    }
}
