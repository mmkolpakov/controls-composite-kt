package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*

/**
 * A reusable, declarative specification for configuring metrics collection for a device member
 * (like a property or action).
 *
 * This [Scheme] provides a standardized way to define static labels and enable/disable
 * specific metrics (e.g., counters, gauges, histograms) for observability backends
 * like Prometheus or OpenTelemetry.
 */
@Serializable(with = MetricsSpec.Serializer::class)
public class MetricsSpec : Scheme() {
    /**
     * A [Meta] block defining a set of static, key-value labels (also known as dimensions)
     * to be attached to all metrics generated for this element.
     * These labels provide rich, queryable context in monitoring systems.
     *
     * Example:
     * ```
     * labels {
     *     "location" put "lab-1"
     *     "rack" put "A3"
     * }
     * ```
     */
    public val labels: MutableMeta by lazy { meta.getOrCreate("labels") }

    /**
     * A [Meta] block for enabling, disabling, or configuring specific metrics.
     * The structure of this block is defined by the runtime's metrics implementation.
     *
     * Example:
     * ```
     * configuration {
     *     "invocations_total" put { // Configure a counter metric
     *         "enabled" put true
     *     }
     *     "last_value" put { // Configure a gauge
     *         "enabled" put false // Disable this specific metric
     *     }
     * }
     * ```
     */
    public val configuration: MutableMeta by lazy { meta.getOrCreate("configuration") }

    public companion object : SchemeSpec<MetricsSpec>(::MetricsSpec)
    public object Serializer : SchemeAsMetaSerializer<MetricsSpec>(Companion)
}
