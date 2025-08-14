[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![](https://maven.sciprog.center/api/badge/latest/kscience/space/kscience/controls-core-jvm?color=40c14a&name=repo.kotlin.link&prefix=v)](https://maven.sciprog.center/)

# controls-composite-kt

`controls-composite-kt` is a multiplatform, asynchronous framework written in Kotlin for building declarative, resilient, and distributed control systems and device simulations. It is a complete architectural evolution of the original `controls-kt` project, redesigned from the ground up to support modern, mission-critical applications.

The framework's philosophy is centered around separating a device's "what" (its specification, or `DeviceBlueprint`) from its "how" (its runtime implementation and driver). This declarative approach enables features like static validation, automatic UI generation, transactional operations, and robust state management.

## Key Concepts

-   **Declarative Blueprints (`DeviceBlueprint`)**: Instead of writing imperative code to manage a device, you define its complete structure, properties, actions, and features in a serializable `DeviceBlueprint`. This blueprint acts as a self-contained factory for creating device instances.

-   **Formal Lifecycles (FSM)**: A device's lifecycle is not just `start()` and `stop()`. It's a formal Finite State Machine (FSM) managed by the powerful [KStateMachine](https://github.com/KStateMachine/kstatemachine) library. This provides predictable, robust, and extensible lifecycle management, including states like `Attaching`, `Running`, `Failed`, and `Detaching`.

-   **Reactive State Management (`DeviceState`)**: All device properties are exposed as reactive `DeviceState<T>` objects, which are backed by Kotlin `StateFlow`. A state contains not just a value, but also a high-precision timestamp and a `Quality` indicator, enabling sophisticated, real-time monitoring and logic.

-   **Composite Architecture**: Complex devices are built by composing smaller, reusable child devices. The framework's DSL provides a declarative way to define these parent-child relationships and create reactive `PropertyBinding`s that automatically propagate state changes.

-   **Transactional Plans (`TransactionPlan`)**: For complex, multistep operations that must succeed or fail as a single unit, the framework offers a `plan { ... }` DSL. This creates a serializable plan that supports sequences, parallel execution, and compensating actions (Saga pattern) for reliable rollbacks.

-   **Pluggable Services**: The runtime is built on DataForge's context and plugin system. Core services like persistence (`SnapshotStore`), metrics (`MetricCollector`), and service discovery are pluggable, allowing you to choose implementations that fit your environment.

-   **Type-Safe DSL**: The primary way to define blueprints is through a type-safe Kotlin DSL (`deviceBlueprint { ... }` or `DeviceSpecification` classes). The DSL leverages delegated properties to make defining properties and actions intuitive and concise.

## Modules

### [controls-composite-model](controls-composite-model)
> Pure, platform-agnostic data models, contracts, and serializable message formats that define the core of the system.
>
> **Maturity**: EXPERIMENTAL

### [controls-composite-dsl](controls-composite-dsl)
> A type-safe Kotlin DSL for building composite device specifications (`DeviceBlueprint`). This is the primary user-facing API for defining devices.
>
> **Maturity**: PROTOTYPE

### [controls-composite-metrics](controls-composite-metrics)
> A cross-platform metrics API and a default AtomicFU-based implementation for `Counter`, `Gauge`, and `Histogram`.
>
> **Maturity**: EXPERIMENTAL

### [controls-composite-persistence](controls-composite-persistence)
> A persistence layer for saving and restoring device states, with multiplatform `SnapshotStore` implementations (file-based for JVM/Native, localStorage for JS/Wasm).
>
> **Maturity**: EXPERIMENTAL

### [controls-composite-ports](controls-composite-ports)
> A multiplatform low-level IO `Port` abstraction for raw byte-level communication, serving as a foundation for hardware drivers.
>
> **Maturity**: PROTOTYPE

### [controls-exporter-prometheus](controls-exporter-prometheus)
> A KMP-native Prometheus exporter for metrics collected via the `controls-composite-metrics` module.
>
> **Maturity**: PROTOTYPE

---

## Architectural Comparison: `controls-kt` vs. `controls-composite-kt`

`controls-composite-kt` is a complete architectural redesign of the original `controls-kt` framework. It moves from a flexible but imperative-heavy model to a formal, declarative, and more resilient paradigm. This table details the key conceptual shifts.

| Aspect                         | `controls-kt` (Legacy)                                                                                                                                  | `controls-composite-kt` (New)                                                                                                                                                                                                     | Key Improvement / Rationale                                                                                                                                                                                                                                        |
|:-------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Core Paradigm**              | **Imperative-Reactive**: Devices are created and wired together programmatically. Reactive logic is built manually using `Flow` operators.              | **Declarative & Formal**: Devices are defined as serializable `DeviceBlueprint` models. The runtime instantiates and manages them based on this formal description.                                                               | **Reproducibility & Static Analysis**: A declarative model can be statically validated, stored, and transmitted. It guarantees that a device's structure is consistent and decouples the definition from the implementation.                                       |
| **Device Definition**          | `DeviceSpec` is a `companion object` within a `Device` class, tightly coupling the specification to one implementation.                                 | `DeviceBlueprint` is a standalone, versioned model. The implementation logic is encapsulated in a separate `DeviceDriver`. The DSL uses `DeviceSpecification` classes as builders.                                                | **Separation of Concerns**: The blueprint (the "what") is now fully independent of the driver (the "how"). This allows multiple drivers (e.g., real hardware, simulation) for a single blueprint.                                                                  |
| **State Management**           | Properties are simple types (`Double`, `String`). `CachingDevice` is an optional mix-in. No built-in concept of state quality or timestamp.             | State is represented by `DeviceState<T>`, which wraps a `StateValue<T>`. `StateValue` includes the `value`, a high-precision `timestamp`, and a `Quality` enum (`OK`, `STALE`, `INVALID`, `ERROR`).                               | **Rich & Observable State**: The state model is fundamentally more robust. It provides crucial metadata for resilience, diagnostics, and preventing the use of stale or invalid data in distributed systems.                                                       |
| **Lifecycle Management**       | A simple `WithLifeCycle` interface with `start()` and `stop()` methods. Lifecycle is managed imperatively.                                              | A formal **Finite State Machine (FSM)** via KStateMachine. The lifecycle is defined by states (`Attaching`, `Running`, `Failed`) and driven by `DeviceLifecycleEvent`s.                                                           | **Robustness & Predictability**: The FSM guarantees valid state transitions, prevents race conditions, and makes the device lifecycle explicit, observable, and extensible in a predictable way.                                                                   |
| **Device Composition**         | Imperative. Devices are grouped in a `DeviceManager` or `DeviceGroup`. Property connections are created manually by collecting and re-emitting `Flow`s. | **Declarative**. Child devices are defined in the parent's blueprint using a `child { ... }` DSL. Property connections are declared with `bindings { child.prop bindsTo parent.prop }`.                                           | **Declarative Data Flow**: Radically simplifies the creation of complex devices. It reduces boilerplate code, makes relationships explicit and analyzable, and allows the runtime to manage the reactive graph.                                                    |
| **Complex Operations**         | No built-in concept. Complex logic is implemented as a regular `suspend fun` within an `action`, with no transactional guarantees.                      | **`TransactionPlan`** with a `plan { ... }` DSL. Supports sequences, parallel execution, and compensating actions (**Saga Pattern**) for reliable rollbacks.                                                                      | **Transactional Integrity**: Critical for operations that must succeed or fail as a whole (e.g., system startup, calibration routines). This prevents the system from being left in an inconsistent state.                                                         |
| **Persistence**                | Not a core feature. The `controls-storage` module was a basic, separate implementation.                                                                 | **First-Class Concept**. `StatefulDevice` contract, `SnapshotStore` API, and multiplatform implementations (`File`, `LocalStorage`, `InMemory`). Persistence is configured declaratively.                                         | **Resilience**: A core requirement for long-running systems. Built-in persistence allows devices to recover their state after restarts or failures, greatly improving system robustness.                                                                           |
| **Metrics & Monitoring**       | No built-in support.                                                                                                                                    | **First-Class Concept**. A multiplatform `MetricCollector` API (`Counter`, `Gauge`, `Histogram`) and a pluggable `PrometheusExporter`. Metrics are declared in property/action descriptors.                                       | **Observability**: Modern systems must be observable. This provides a standard, decoupled, and low-overhead way to instrument device logic and integrate with industry-standard monitoring tools.                                                                  |
| **Communication**              | `DeviceMessage` as a `sealed class`. Relies heavily on a `MagixEndpoint`. `PeerConnection` for binaries was a later addition.                           | `DeviceMessage` as a `sealed interface` for better extensibility. Abstracted `MessageBroker` for events and a more formal `PeerConnection` contract for binary data.                                                              | **Formalized Protocols**: The separation of a general-purpose `MessageBroker` for events from a `PeerConnection` for direct data transfer is a cleaner, more scalable design that aligns well with CQRS principles.                                                |
| **Simulation Model**           | The `controls-constructor` module provides a separate DSL for building simulations. Simulated devices are conceptually different from "real" ones.      | **Unified Model**. Simulation is not a separate concept. A simulated device is simply a `DeviceBlueprint` with a `DeviceDriver` that implements simulation logic instead of hardware I/O. The same DSL is used for both.          | **Seamless Substitution**: Eliminates the artificial distinction between real and simulated devices. This allows a simulation to be replaced by a real device (or vice versa) by simply changing the driver, without altering the blueprint or any consuming code. |
| **Integration with DataForge** | `controls-kt` is built as an application on top of DataForge, using its `Context`, `Plugin`, and `Meta` systems.                                        | `controls-composite-kt` uses the same foundational DataForge components but formalizes its integration points via `Feature`s like `DataSourceFeature` and `TaskExecutorFeature`. A `Device` can be exposed as a `DataTree<Meta>`. | **Explicit Integration**: The new framework makes its integration points with other systems (like `dataforge-workspace`) explicit and part of its static contract. This makes the system more modular and easier to reason about.                                  |

