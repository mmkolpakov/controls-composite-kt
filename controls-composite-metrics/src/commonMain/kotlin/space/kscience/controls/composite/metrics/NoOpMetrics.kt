package space.kscience.controls.composite.metrics

/**
 * A no-op implementation of [Counter] that does nothing and always returns 0.
 * This is used when metrics are disabled to avoid performance overhead.
 *
 * @param name The name of the counter, used for identification but not stored.
 */
public class NoOpCounter(name: String) : Counter(MetricId(name), null, 0L) {
    override fun inc(delta: Long) {
        // no-op
    }
}

/**
 * A no-op implementation of [Gauge] that does nothing and always returns 0.0.
 * This is used when metrics are disabled to avoid performance overhead.
 *
 * @param name The name of the gauge, used for identification but not stored.
 */
public class NoOpGauge(name: String) : Gauge(MetricId(name), null, 0.0) {
    override var value: Double
        get() = 0.0
        set(_) {
            // no-op
        }
}

/**
 * A no-op implementation of [Histogram] that does nothing.
 */
public class NoOpHistogram(name: String) : Histogram(MetricId(name), null, emptyList()) {
    override fun observe(value: Double) {
        // no-op
    }
}


/**
 * A no-op implementation of [MetricCollector] that does not collect any metrics.
 * This is used by runtime components when [MetricPlugin] is not installed in the context,
 * making metrics collection fully optional.
 */
public object NoOpMetricCollector : MetricCollector {
    override val name: String = "no-op"

    override fun counter(id: MetricId, help: String?, initialValue: Long): Counter = NoOpCounter(id.name)

    override fun gauge(id: MetricId, help: String?, initialValue: Double): Gauge = NoOpGauge(id.name)

    override fun histogram(id: MetricId, help: String?, buckets: List<Double>): Histogram = NoOpHistogram(id.name)

    override fun remove(metric: Metric) {
        // no-op
    }

    override fun report(): Map<MetricId, Metric> = emptyMap()
}