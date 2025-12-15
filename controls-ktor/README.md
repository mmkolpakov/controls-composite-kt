# Module controls-ktor

**Maturity**: PROTOTYPE

## Description

This module provides multiplatform implementations of the `Port` and `PeerConnection` contracts from `controls-model` and `controls-ports` using the Ktor networking library. It serves as a foundational transport layer for building device drivers that communicate over TCP or UDP.

## Key Features

- **`KtorTcpPort` & `KtorUdpPort`**: `Port` implementations for TCP client sockets and UDP sockets, respectively.
- **`KtorTcpPeerConnection`**: A `PeerConnection` implementation for direct, efficient, peer-to-peer binary data transfer over TCP.
- **Pluggable Architecture**: The module includes `KtorPortsPlugin` and `KtorPeerPlugin`, allowing the `PortManager` and other services to automatically discover and instantiate these components from configuration.

## Usage

This module is typically used by `DeviceDriver` implementations or runtime services, not directly by application code. You can configure a device to use these components via a `Meta` block:

### For Ports

```kotlin
// In a device driver's attachment meta
meta {
    "port" put {
        "type" put "ktor.tcp" // or "ktor.udp"
        "host" put "192.168.1.100"
        "port" put 1234
    }
}
```

### For Peer Connections (Plugin)

```kotlin
// In the main context configuration
plugin(KtorPeerPlugin) {
    "port" put 8080 // Port for the peer-to-peer server to listen on
}
```