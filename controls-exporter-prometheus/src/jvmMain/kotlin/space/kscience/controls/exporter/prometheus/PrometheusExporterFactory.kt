package space.kscience.controls.exporter.prometheus

import space.kscience.controls.composite.metrics.MetricExporter
import space.kscience.controls.composite.metrics.MetricExporterFactory
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

/**
 * A factory for creating [PrometheusExporter] instances.
 * This object is intended to be registered in the DataForge context
 * to allow the [MetricsExporterPlugin] to discover and use this implementation.
 */
public object PrometheusExporterFactory : MetricExporterFactory {
    override val type: String = "prometheus"

    override fun create(context: Context, meta: Meta): MetricExporter = PrometheusExporter(context, meta)
}