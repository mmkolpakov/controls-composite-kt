# Module controls-composite-protocol-modbus

**Maturity**: PROTOTYPE

## Description

This module provides a concrete implementation of the `ProtocolAdapter` contract for the Modbus protocol, using the `j2mod` library. This allows any device blueprint to communicate over Modbus without requiring a custom `DeviceDriver`.

## Usage

This module is typically not used directly in application code. Instead, it is included as a dependency, and the `ModbusProtocolAdapter` is referenced from within a `DeviceBlueprint`'s metadata.

**Example `DeviceBlueprint` configuration:**
```kotlin
object MyModbusSensorSpec : DeviceSpecification<Device>() {
    val temperature by doubleProperty(
        read = { error("Should be handled by ProtocolDeviceDriver") }
    ) {
        // Modbus-specific configuration for the adapter
        meta {
            "modbus.unitId" put 1
            "modbus.type" put "inputRegister"
            "modbus.address" put 100
            "modbus.converter" put "float32" // Example for multi-register value
        }
    }

    override fun CompositeSpecBuilder<Device>.configure() {
        // Use the universal protocol driver
        driver(ProtocolDeviceDriver(
            port = context.ports.newPort {
                "type" put "ktor.tcp"
                "host" put "192.168.1.10"
                "port" put 502
            },
            adapter = context.plugins.find(true) { it is ModbusProtocolAdapter } as ModbusProtocolAdapter
        ))
    }
}
```