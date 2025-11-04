# Module magix-transport-rsocket-tcp

**Maturity**: PROTOTYPE

## Description

This module provides an implementation of the `MagixEndpoint` contract using RSocket over a raw TCP transport. It is intended for JVM and Native environments where direct TCP connections are available.

## Key Features

- **`RSocketWithTcpEndpointFactory`**: A `MagixEndpointFactory` that can be discovered by a `MagixBrokerPlugin` to create TCP-based RSocket endpoints.
- **Platform Specific**: This implementation is not available for browser or WasmJs targets.

## Usage

This module is typically not used directly but is included as a dependency in an application that uses a `MagixBrokerPlugin`. The plugin will automatically discover the factory provided by this module.

**Configuration via `Meta`:**
```kotlin
plugin(MagixBrokerPlugin) {
    "endpoint" {
        "type" put "rsocket.tcp"
        "host" put "localhost"
        "port" put 7778
    }
}
```