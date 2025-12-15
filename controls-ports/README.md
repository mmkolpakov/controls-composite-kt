# Module controls-ports

**Maturity**: PROTOTYPE

## Description

This module provides a multiplatform, low-level I/O abstraction called `Port`. It serves as a unified interface for **discrete, message-based** raw byte-level communication, abstracting away the details of specific transports like TCP, UDP, or serial connections.

This contract is complemented by the `StreamPort` interface (defined in `controls-model`), which is designed for **continuous, low-latency** data flows and is built upon `kotlinx-io` for maximum performance.

Together, `Port` (from this module) and `StreamPort` provide a comprehensive I/O foundation for building device drivers, allowing developers to choose the right abstraction for either message-oriented or stream-oriented hardware.

## Key Features

- **`Port` Interface**: A simple, lifecycle-aware contract for sending (`send(ByteArray)`) and receiving (`receive(): Flow<ByteArray>`) discrete chunks of raw byte data.
- **`SynchronousPort` Interface**: An extension of `Port` for request-response style communication.
- **`PortManager` Plugin**: A DataForge plugin that acts as a factory for creating and managing `Port` instances from configuration, allowing for pluggable transport implementations.