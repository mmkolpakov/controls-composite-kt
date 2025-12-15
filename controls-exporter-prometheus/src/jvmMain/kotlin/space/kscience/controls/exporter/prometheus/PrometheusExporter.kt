package space.kscience.controls.exporter.prometheus

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import space.kscience.controls.composite.metrics.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.info
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.long
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds

/**
 * A JVM-specific implementation of a Prometheus exporter that correctly handles the Prometheus data old.
 *
 * This exporter operates in Pull Mode by running a lightweight Ktor/Netty server that exposes a `/metrics` endpoint.
 * To ensure correctness and performance, it maintains its own internal state:
 * - It periodically polls the [MetricCollector] in a background coroutine.
 * - For **Gauges**, it stores the latest value.
 * - For **Counters**, it correctly handles the monotonic nature of Prometheus counters. It tracks the last seen
 *   value from the source and only exposes the accumulated delta, preventing negative rates if the source
 *   counter is reset.
 * - For **Histograms**, it mirrors the state of the source histogram, including sum, count, and bucket values.
 *
 * The `/metrics` endpoint serves a text representation generated from this internal state, making HTTP requests fast
 * and non-blocking.
 *
 * ### Configuration via [Meta]:
 * - `port`: The port for the metrics server (default: 9091).
 * - `updateInterval`: The interval in milliseconds for polling the [MetricCollector] (default: 5000).
 */
public class PrometheusExporter(private val context: Context, private val meta: Meta) : MetricExporter {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(context.coroutineContext + job)
    private val server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    // --- Internal state for Prometheus-compatible values ---
    private val prometheusGauges = ConcurrentHashMap<MetricId, Double>()
    private val prometheusCounters = ConcurrentHashMap<MetricId, Long>()
    private val prometheusHistograms = ConcurrentHashMap<MetricId, HistogramSnapshot>()

    // State for tracking source counter changes
    private val lastSourceCounterValues = ConcurrentHashMap<MetricId, Long>()

    // Cache for metric metadata (type and help text)
    private val metricMetadata = ConcurrentHashMap<String, Pair<String, String?>>()

    private data class HistogramSnapshot(
        val sum: Double,
        val count: Long,
        val buckets: List<Double>,
//        TODO Property with 'Array' type in a 'data' class: it is recommended to override 'equals()' and 'hashCode()'
        val bucketCounts: LongArray,
    )

    init {
        val port = meta["port"].int ?: 9091
        server = embeddedServer(Netty, port = port, module = { metricsModule() })
    }

    private fun Application.metricsModule() {
        routing {
            get("/metrics") {
                val formattedText = formatMetricsAsPrometheusText()
                call.respondText(formattedText)
            }
        }
    }

    override fun start(collector: MetricCollector) {
        val updateInterval = meta["updateInterval"].long ?: 5000L
        context.logger.info { "Starting PrometheusExporter on port ${meta["port"].int ?: 9091} with update interval ${updateInterval}ms" }

        scope.launch {
            while (isActive) {
                updateInternalState(collector)
                delay(updateInterval.milliseconds)
            }
        }

        server.start(wait = false)
    }

    override fun stop() {
        job.cancel("PrometheusExporter stopped.")
        server.stop(1000, 2000)
        context.logger.info { "PrometheusExporter stopped." }
    }

    private fun updateInternalState(collector: MetricCollector) {
        collector.report().forEach { (_, metric) ->
            when (metric) {
                is Gauge -> {
                    prometheusGauges[metric.id] = metric.value
                    metricMetadata.putIfAbsent(metric.name, "gauge" to metric.help)
                }
                is Counter -> {
                    val currentValue = metric.value
                    val lastValue = lastSourceCounterValues.getOrDefault(metric.id, 0L)
                    val currentPrometheusValue = prometheusCounters.getOrDefault(metric.id, 0L)

                    if (currentValue > lastValue) {
                        val delta = currentValue - lastValue
                        prometheusCounters[metric.id] = currentPrometheusValue + delta
                    } else if (currentValue < lastValue) {
                        context.logger.warn { "Source counter '${metric.name}' with labels ${metric.labels} was reset (from $lastValue to $currentValue). The Prometheus counter will not be decremented." }
                    }
                    lastSourceCounterValues[metric.id] = currentValue
                    metricMetadata.putIfAbsent(metric.name, "counter" to metric.help)
                }
                is Histogram -> {
                    prometheusHistograms[metric.id] = HistogramSnapshot(
                        sum = metric.sum,
                        count = metric.value,
                        buckets = metric.buckets,
                        bucketCounts = metric.getBucketCounts(),
                    )
                    metricMetadata.putIfAbsent(metric.name, "histogram" to metric.help)
                }
            }
        }
    }

    private fun formatMetricsAsPrometheusText(): String = buildString {
        val gaugesByName = prometheusGauges.entries.groupBy { it.key.name }
        val countersByName = prometheusCounters.entries.groupBy { it.key.name }
        val histogramsByName = prometheusHistograms.entries.groupBy { it.key.name }

        val allMetricNames = (gaugesByName.keys + countersByName.keys + histogramsByName.keys).sorted()

        for (metricName in allMetricNames) {
            val sanitizedName = sanitizeMetricName(metricName)
            val (type, help) = metricMetadata[metricName] ?: continue

            appendLine("# TYPE $sanitizedName $type")
            if (help != null && help.isNotBlank()) {
                appendLine("# HELP $sanitizedName ${help.replace("\n", " ")}")
            }

            when (type) {
                "gauge" -> gaugesByName[metricName]?.forEach { (id, value) -> appendTimeseries(sanitizedName, id, value.toString()) }
                "counter" -> countersByName[metricName]?.forEach { (id, value) -> appendTimeseries(sanitizedName, id, value.toString()) }
                "histogram" -> histogramsByName[metricName]?.forEach { (id, snapshot) ->
                    var cumulativeCount = 0L
                    for (i in snapshot.buckets.indices) {
                        cumulativeCount += snapshot.bucketCounts[i]
                        val bucketLabels = id.labels + ("le" to formatBucket(snapshot.buckets[i]))
                        appendTimeseries("${sanitizedName}_bucket", id.copy(labels = bucketLabels), cumulativeCount.toString())
                    }
                    appendTimeseries("${sanitizedName}_sum", id, snapshot.sum.toString())
                    appendTimeseries("${sanitizedName}_count", id, snapshot.count.toString())
                }
            }
        }
    }

    private fun StringBuilder.appendTimeseries(name: String, id: MetricId, value: String) {
        append(name)
        if (id.labels.isNotEmpty()) {
            append("{")
            id.labels.entries.joinTo(this, ",") { (key, labelValue) ->
                val sanitizedKey = sanitizeLabelName(key)
                val escapedValue = labelValue.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                "$sanitizedKey=\"$escapedValue\""
            }
            append("}")
        }
        append(" ")
        appendLine(value)
    }

    private fun formatBucket(value: Double): String = when (value) {
        Double.POSITIVE_INFINITY -> "+Inf"
        else -> value.toString()
    }

    private fun sanitizeMetricName(name: String): String {
        val sanitized = name.replace(Regex("[^a-zA-Z0-9_:]"), "_")
        return if (sanitized.firstOrNull()?.isLetter() == true || sanitized.firstOrNull() in listOf('_', ':')) {
            sanitized
        } else {
            "_$sanitized"
        }
    }

    private fun sanitizeLabelName(name: String): String {
        val sanitized = name.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return if (sanitized.firstOrNull()?.isLetter() == true || sanitized.firstOrNull() == '_') {
            sanitized
        } else {
            "_$sanitized"
        }
    }
}