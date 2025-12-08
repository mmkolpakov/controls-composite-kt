package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*

/**
 * A reusable, declarative specification for configuring telemetry, with a primary focus on
 * distributed tracing (e.g., OpenTelemetry).
 *
 * This [Scheme] provides fine-grained control over tracing for specific device members,
 * allowing developers to enable or disable it and to add static, queryable attributes to trace spans.
 */
@Serializable(with = TelemetrySpec.Serializer::class)
public class TelemetrySpec : Scheme() {
    /**
     * Explicitly enables or disables tracing for this specific element.
     * If `null` (the default), the decision is deferred to the runtime's global or default policy.
     * This three-state logic (`true`, `false`, `null`) allows for both explicit control and
     * adherence to system-wide defaults.
     */
    public var tracingEnabled: Boolean? by boolean()

    /**
     * A [Meta] block of static key-value attributes to be added to every trace span
     * generated for this element. These attributes provide rich, queryable context in
     * observability platforms like Jaeger or Grafana Tempo.
     *
     * This is analogous to setting static attributes on an OpenTelemetry Span.
     *
     * Example:
     * ```
     * attributes {
     *     "device.id" put "motor-123"
     *     "operation.criticality" put "high"
     * }
     * ```
     */
    public val attributes: MutableMeta by lazy { meta.getOrCreate("attributes") }

    public companion object : SchemeSpec<TelemetrySpec>(::TelemetrySpec)
    public object Serializer : SchemeAsMetaSerializer<TelemetrySpec>(Companion)
}
