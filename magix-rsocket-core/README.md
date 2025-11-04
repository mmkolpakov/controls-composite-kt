# Module magix-rsocket-core

**Maturity**: PROTOTYPE

## Description

This module provides the core, **transport-agnostic** logic for implementing `MagixEndpoint` using the RSocket protocol. It serves as a foundational layer for specific RSocket transport implementations like TCP or WebSockets.

The main component, `RSocketMagixEndpoint`, is a generic client that wraps a connected `RSocket` instance, providing the standard Magix API for broadcasting messages and creating subscriptions. This module does not handle network connections itself; it delegates this responsibility to transport-specific modules.

## Key Features

- **`RSocketMagixEndpoint`**: A multiplatform implementation of the `MagixEndpoint` interface that operates over an existing `RSocket` connection.
    - Uses **Request-Stream** for subscriptions (`subscribe`).
    - Uses **Fire-and-Forget** for broadcasting messages (`broadcast`).
- **Transport Agnostic**: Designed to work with any underlying RSocket transport (TCP, WebSockets, etc.) by accepting a provider function for a connected `RSocket`.
- **Configuration Utilities**: Includes a helper function (`configureFromMeta`) to simplify the configuration of an `RSocketConnector` from a DataForge `Meta` object.

## Relationship to Other Modules

- **Implements**: `magix-api` (provides a concrete implementation of `MagixEndpoint`).
- **Used by**:
    - `magix-transport-rsocket-tcp`: Uses this module to create a TCP-based Magix endpoint.
    - `magix-transport-rsocket-ws`: Uses this module to create a WebSocket-based Magix endpoint.

## Usage

This module is primarily intended for internal use by other `magix-transport-*` modules, not for direct use in application code. The typical flow is that a transport-specific factory (like `RSocketWithTcpEndpointFactory`) will create a connected `RSocket` and then pass it to the `RSocketMagixEndpoint` to create the final endpoint.

**Example:**

```kotlin
// 1. Create a transport (e.g., TCP) and an RSocket connector
val transport = KtorTcpClientTransport(coroutineContext)
val connector = RSocketConnector { /* ... configuration ... */ }

// 2. Define a provider that connects when called
val rsocketProvider: suspend () -> RSocket = {
    connector.connect(transport.target(host, port))
}

// 3. Create the MagixEndpoint using the provider from this module
val magixEndpoint = RSocketMagixEndpoint.fromProvider(
    coroutineContext,
    rsocketProvider
)

// The endpoint is now ready to be connected and used
// magixEndpoint.connect()
```