package space.kscience.controls.composite.metrics

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlin.time.Clock
import kotlin.time.DurationUnit

/**
 * A type-safe, unique identifier for a metric instance, combining its name and a set of key-value labels.
 * This class is crucial for distinguishing between different time series of the same metric.
 * For example, `http_requests_total{method="GET"}` and `http_requests_total{method="POST"}`
 * are two distinct metric instances identified by different `MetricId`s.
 *
 * @property name The name of the metric (e.g., "http_requests_total").
 * @property labels An immutable map of label key-value pairs.
 */
public data class MetricId(val name: String, val labels: Map<String, String> = emptyMap())

/**
 * A base interface for a metric value that can be collected. Each metric instance is uniquely
 * identified by its [MetricId].
 *
 * @property id The unique identifier for this metric instance.
 * @property help An optional human-readable description of the metric, used for the `HELP` line in Prometheus.
 * @property value The current numeric value of the metric. For complex types like `Histogram`, this is the total count.
 */
public sealed interface Metric {
    public val id: MetricId
    public val help: String?
    public val value: Number
    public val name: String get() = id.name
    public val labels: Map<String, String> get() = id.labels
}

/**
 * A metric that represents a monotonically increasing value.
 *
 * @param id The unique identifier for this counter.
 * @param help An optional description for Prometheus.
 * @param initialValue The starting value of the counter.
 */
public open class Counter(
    override val id: MetricId,
    override val help: String? = null,
    initialValue: Long = 0L,
) : Metric {
    private val _value = atomic(initialValue)
    override val value: Long get() = _value.value

    public open fun inc(delta: Long = 1L) {
        require(delta >= 0) { "Counter can only be incremented with non-negative values." }
        _value.addAndGet(delta)
    }
}

/**
 * A metric that represents a value that can go up and down.
 *
 * @param id The unique identifier for this gauge.
 * @param help An optional description for Prometheus.
 * @param initialValue The starting value of the gauge.
 */
public open class Gauge(
    override val id: MetricId,
    override val help: String? = null,
    initialValue: Double = 0.0,
) : Metric {
    private val _value = atomic(initialValue)
    public override var value: Double
        get() = _value.value
        set(newValue) {
            _value.value = newValue
        }
}

/**
 * A metric that samples observations into configurable buckets.
 * It is typically used for measuring latencies or request sizes.
 *
 * @param id The unique identifier for this histogram.
 * @param help An optional description for Prometheus.
 * @param buckets A list of upper bounds for the observation buckets. The list will be sorted internally.
 *                Prometheus also requires a `+Inf` bucket, which is added automatically.
 */
public open class Histogram(
    override val id: MetricId,
    override val help: String? = null,
    buckets: List<Double>,
) : Metric {
    public val buckets: List<Double> = (buckets.sorted() + Double.POSITIVE_INFINITY)
    private val bucketCounts = Array(this.buckets.size) { atomic(0L) }
    private val _sum = atomic(0.0)
    private val _count = atomic(0L)

    override val value: Long get() = _count.value
    public val sum: Double get() = _sum.value

    /**
     * Returns a snapshot of the current bucket counts.
     * The returned array has the same size as [buckets], where each element
     * represents the count of observations for the corresponding bucket.
     */
    public fun getBucketCounts(): LongArray = LongArray(bucketCounts.size) { bucketCounts[it].value }

    /**
     * Records a new observation.
     * The method finds the appropriate bucket for the value and increments its count,
     * as well as the total sum and count of observations.
     *
     * @param value The value of the observation.
     */
    public open fun observe(value: Double) {
        // Find the correct bucket index using binarySearch on the List.
        // `binarySearch` returns the index of the element, or `(-insertionPoint - 1)`.
        val index = this.buckets.binarySearch(value)
        val bucketIndex = if (index >= 0) {
            index
        } else {
            -index - 1
        }

        // It is guaranteed that bucketIndex will be in bounds because `+Inf` is always the last bucket.
        if (bucketIndex < bucketCounts.size) {
            bucketCounts[bucketIndex].incrementAndGet()
        }

        _count.incrementAndGet()
        _sum.update { it + value }
    }
}

/**
 * A generic collector for various types of metrics.
 */
public interface MetricCollector {
    public val name: String

    public fun counter(id: MetricId, help: String? = null, initialValue: Long = 0L): Counter
    public fun gauge(id: MetricId, help: String? = null, initialValue: Double = 0.0): Gauge
    public fun histogram(id: MetricId, help: String? = null, buckets: List<Double>): Histogram

    public fun remove(metric: Metric)

    /**
     * Returns a map of all registered metrics, keyed by their unique [MetricId].
     * This is the main method for exporters to get data.
     */
    public fun report(): Map<MetricId, Metric>

    public companion object
}

/**
 * An KMP-safe, in-memory implementation of [MetricCollector] using atomicfu.
 */
public class AtomicMetricCollector(override val name: String) : MetricCollector {
    private val metrics = atomic(emptyMap<MetricId, Metric>())

    override fun counter(id: MetricId, help: String?, initialValue: Long): Counter =
        (metrics.value[id] as? Counter) ?: Counter(id, help, initialValue).also { metric ->
            metrics.update { it + (id to metric) }
        }

    override fun gauge(id: MetricId, help: String?, initialValue: Double): Gauge =
        (metrics.value[id] as? Gauge) ?: Gauge(id, help, initialValue).also { metric ->
            metrics.update { it + (id to metric) }
        }

    override fun histogram(id: MetricId, help: String?, buckets: List<Double>): Histogram =
        (metrics.value[id] as? Histogram) ?: Histogram(id, help, buckets).also { metric ->
            metrics.update { it + (id to metric) }
        }

    override fun remove(metric: Metric) {
        metrics.update { it - metric.id }
    }

    override fun report(): Map<MetricId, Metric> = metrics.value
}

/**
 * Executes a block of code and records its execution time to a [Histogram] metric in seconds.
 *
 * @param histogram The histogram to record the duration to.
 * @param clock The clock to use for time measurement, defaults to [Clock.System]. Crucial for simulations.
 * @return The result of the [block].
 */
public suspend fun <R> timed(histogram: Histogram, clock: Clock = Clock.System, block: suspend () -> R): R {
    val start = clock.now()
    val result = block()
    val duration = clock.now() - start
    histogram.observe(duration.toDouble(DurationUnit.SECONDS))
    return result
}