# Module controls-exporter-prometheus

**Maturity**: PROTOTYPE

## Description

This module provides a robust, multiplatform Prometheus exporter for metrics collected via the `controls-composite-metrics` API. It allows you to easily expose the internal state and performance indicators of your composite devices to a Prometheus monitoring system.

The exporter runs a lightweight Ktor-based HTTP server that exposes a standard `/metrics` endpoint, which Prometheus can scrape (pull).

## Key Features

- **Prometheus Pull Model**: Implements the standard pull-based monitoring approach by running an embedded HTTP server.
- **Correct Metric Handling**:
    - **Counters**: Correctly handles the monotonic nature of Prometheus counters. It tracks the last seen value from the application's counter and only exposes the accumulated delta. This prevents issues with rate calculations if the source counter is reset (e.g., on application restart).
    - **Gauges**: Directly mirrors the latest value of the application's gauge.
    - **Histograms**: Translates the internal histogram state (sum, count, and bucket counts) into the standard Prometheus histogram format (`_bucket`, `_sum`, `_count`).
- **Efficient & Non-Blocking**: The exporter polls the `MetricCollector` periodically in the background. The HTTP handler for `/metrics` serves a pre-formatted text response from an internal cache, making scrape requests extremely fast and non-blocking for your application's critical path.
- **Pluggable Architecture**: Implemented as a `MetricExporterFactory`, it can be dynamically discovered and configured by a `MetricsExporterPlugin` in a DataForge context.
- **JVM-Native**: The primary implementation uses Ktor/Netty for high performance on the JVM.

## Configuration

The exporter is configured via a `Meta` block when it's created.

```kotlin
// Example configuration in a DataForge context plugin
plugin(MetricsExporterPlugin) {
    exporter("prometheus") {
        "port" put 9091 // The port for the metrics server (default: 9091)
        "updateInterval" put 5000 // Interval in ms to poll the collector (default: 5000)
    }
}
```
- `port`: The TCP port on which the `/metrics` endpoint will be exposed.
- `updateInterval`: The interval in milliseconds at which the exporter will poll the `MetricCollector` to update its internal state.