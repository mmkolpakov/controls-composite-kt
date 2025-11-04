# Module controls-composite-protocol-api

**Maturity**: PROTOTYPE

## Description

This module provides a universal API layer for protocol adapters, decoupling device logic from transport protocols. It defines the core `ProtocolAdapter` contract, which serves as a bridge between a generic `DeviceDriver` and a low-level `Port`.

This abstraction allows for the creation of reusable protocol implementations (like Modbus, OPC-UA, etc.) that can be applied to any device blueprint without requiring a new driver for each piece of hardware.