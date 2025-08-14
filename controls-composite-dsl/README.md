# Module controls-composite-dsl

**Maturity**: PROTOTYPE

## Description

This module provides a powerful, type-safe, and declarative Domain-Specific Language (DSL) for constructing `DeviceBlueprint` instances. It acts as a "builder" layer on top of the pure `controls-composite-model`, allowing developers to define complex devices in an intuitive and readable way.

## Key Features

- **Type-safe Declarations**: Define device properties (`booleanProperty`, `doubleProperty`, etc.) and actions (`unitAction`, `metaAction`) with full type safety using Kotlin's delegated properties.
- **Declarative Composition**: Build complex devices by composing smaller, reusable child device blueprints using the `child` delegate.
- **Formal Lifecycle**: Define a device's lifecycle as a formal Finite State Machine (FSM) using the powerful KStateMachine DSL via the `standardLifecycle` or `lifecycle` blocks.
- **Property Bindings**: Declaratively bind properties of child devices to parent properties or constant values, creating reactive data flows.
- **Transactional Plans**: Create complex, multi-step action plans (`plan { ... }`) that can be serialized and executed by a runtime.
- **Stateful Properties**: Manage internal, logical, and persistent state within a device using the `stateful` delegate.

## Usage

The primary entry point to the DSL is the `compositeDevice` function, which allows you to define a `DeviceBlueprint`. Alternatively, you can use the `DeviceSpecification` base class for a more object-oriented approach to defining reusable specifications.