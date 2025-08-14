# Module controls-composite-ports

**Maturity**: PROTOTYPE

## Description

This module provides a multiplatform, low-level I/O abstraction called `Port`. It serves as a unified interface for raw byte-level communication, abstracting away the details of specific transports like TCP, UDP, or serial connections. This is the foundation upon which drivers for hardware devices are built.

## Key Features

- **`Port` Interface**: A simple, lifecycle-aware contract for sending (`send(ByteArray)`) and receiving (`receive(): Flow<ByteArray>`) raw byte data.
- **`SynchronousPort` Interface**: An extension of `Port` for request-response style communication, guaranteeing that only one request is active at a time.
- **`PortManager` Plugin**: A DataForge plugin that acts as a factory for creating and managing `Port` instances from configuration, allowing for pluggable transport implementations.