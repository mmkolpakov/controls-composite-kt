package space.kscience.controls.composite.metrics

import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A universal interface for any metric exporter.
 * The exporter is responsible for converting and sending data from a single [MetricCollector]
 * to an external monitoring system.
 */
public interface MetricExporter {
    /**
     * Starts the export process. This may involve launching a server,
     * establishing a connection, or subscribing to events.
     *
     * @param collector The source of metrics for export.
     */
    public fun start(collector: MetricCollector)

    /**
     * Stops the export process and releases all associated resources.
     */
    public fun stop()
}

/**
 * A factory for creating instances of [MetricExporter] based on configuration.
 * This allows for a plug-in architecture for metric backends.
 */
public interface MetricExporterFactory {
    /**
     * The name of the exporter type, used in configuration (e.g., "prometheus", "opentelemetry").
     */
    public val type: String

    /**
     * Creates an instance of [MetricExporter].
     *
     * @param context The context in which the exporter will operate.
     * @param meta The configuration specific to this exporter instance.
     */
    public fun create(context: Context, meta: Meta): MetricExporter
}