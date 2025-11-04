# Module magix-transport-rsocket-ws

**Maturity**: PROTOTYPE

## Description

This module provides a multiplatform implementation of the `MagixEndpoint` contract using RSocket over WebSockets for transport. It is suitable for all platforms, including browsers.

## Key Features

- **`RSocketWithWebSocketsEndpointFactory`**: A `MagixEndpointFactory` that can be discovered by a `MagixBrokerPlugin` to create WebSocket-based RSocket endpoints.
- **Multiplatform**: Works on JVM, JS (Browser & Node.js), Native, and WasmJs.

## Usage

This module is typically not used directly but is included as a dependency in an application that uses a `MagixBrokerPlugin`. The plugin will automatically discover the factory provided by this module.

**Configuration via `Meta`:**
```kotlin
plugin(MagixBrokerPlugin) {
    "endpoint" {
        "type" put "rsocket.ws"
        "host" put "localhost"
        "port" put 7777
        "path" put "/rsocket" // optional
    }
}
```

TODO -> 
```
> Task :magix-transport-rsocket-ws:wasmJsPublicPackageJson UP-TO-DATE
Transitive npm dependency version clash for compilation "wasmJsMain"
Candidates:
ws@8.18.3
ws@8.18.0
Selected:
ws@8.18.3
```