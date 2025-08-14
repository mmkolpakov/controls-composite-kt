# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 1.0.0-alpha-1 - 2025-08-15

Initial release of the `controls-composite-kt` framework. This version represents an architectural redesign and a paradigm shift from the previous `controls-kt` project, moving towards a more formal, declarative, and resilient model for building control systems.

### Added

-   **Core Model (`controls-composite-model`)**:
    -   Introduced `DeviceBlueprint`: A declarative, serializable, and versioned model for defining a device's complete structure, behavior, and features, separating the specification from the runtime implementation.
    -   Formalized `Device` as a runtime contract with distinct `PropertyDevice` and `ActionDevice` capabilities.
    -   Established a standardized, serializable `DeviceMessage` sealed hierarchy for all system communication, including property changes, lifecycle events, and errors.
    -   Defined `DevicePropertySpec` and `DeviceActionSpec` for static, type-safe description of a device's public API.
    -   Introduced `DeviceState<T>` and `MutableDeviceState<T>`: A reactive, observable state model containing a `StateValue<T>` which includes the value, a high-precision timestamp, and a `Quality` enum (`OK`, `STALE`, `INVALID`, `ERROR`).

-   **Domain-Specific Language (`controls-composite-dsl`)**:
    -   Created a type-safe Kotlin DSL for building `DeviceBlueprint` instances (`deviceBlueprint { ... }`) and reusable `DeviceSpecification` classes.
    -   Implemented delegated properties for effortless declaration of properties (`property`, `mutableProperty`, `stateProperty`, `derived`) and actions (`action`, `unitAction`).
    -   Added `child` and `children` DSL for declarative composition of devices, including `bindings` blocks for reactive property connections (`bindsTo`).
    -   Introduced `standardLifecycle` and `lifecycle` blocks for defining a device's lifecycle as a formal Finite State Machine (FSM) using KStateMachine.
    -   Added `plan { ... }` DSL for creating complex, multistep, transactional `TransactionPlan`s with support for compensating actions (Saga pattern).

-   **State Management and Persistence (`controls-composite-persistence`)**:
    -   Defined the `StatefulDevice` contract for devices that support state snapshotting and restoration.
    -   Introduced the `SnapshotStore` interface for persistence backends.
    -   Provided multiplatform implementations: `FileSnapshotStore` (JVM/Native), `LocalStorageSnapshotStore` (JS/WasmJs), and `InMemorySnapshotStore` (common).
    -   Added a `PersistencePlugin` for easy integration and configuration.

-   **Metrics and Monitoring (`controls-composite-metrics` & `controls-exporter-prometheus`)**:
    -   Created the `MetricCollector` API for instrumenting code with `Counter`, `Gauge`, and `Histogram` metrics.
    -   Provided `AtomicMetricCollector` as a high-performance, in-memory implementation.
    -   Introduced the `MetricExporter` API and `PrometheusExporterFactory` for pluggable monitoring backends.
    -   Implemented a JVM-native Prometheus exporter that serves metrics via a non-blocking Ktor server.

-   **Low-Level IO (`controls-composite-ports`)**:
    -   Created a multiplatform `Port` and `SynchronousPort` API for raw byte-level communication, abstracting over transports like TCP or Serial.
    -   Added a `PortManager` plugin for discovering and creating `Port` instances from configuration.