# Module controls-composite-model

**Maturity**: EXPERIMENTAL

## Description

This module forms the foundational layer of the `controls-composite-kt` framework. It contains the core, platform-agnostic data models, contracts (interfaces), and serializable message formats that define the entire system. This layer has **zero dependencies on implementation details**, making it a pure, portable definition of the control system's concepts.

## Key Features

- **Device Contracts**: Defines the essential `Device`, `DeviceBlueprint`, and `CompositeDeviceHub` interfaces, establishing the public API for all components.
- **Serializable Messages**: Contains the `DeviceMessage` sealed hierarchy, providing a standard, serializable format for all communication within the system.
- **Declarative Specifications**: Includes `DevicePropertySpec` and `DeviceActionSpec` for statically defining the capabilities of a device.
- **Lifecycle Model**: Provides the formal `DeviceLifecycleState` and `DeviceLifecycleEvent` models, which are used by the KStateMachine-based lifecycle management.
- **State Primitives**: Defines the reactive `DeviceState` and `MutableDeviceState` interfaces, which are the fundamental building blocks for device properties in the `controls-composite-dsl` module.