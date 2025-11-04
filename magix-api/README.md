# Module magix-api

**Maturity**: EXPERIMENTAL

## Description

This module provides the core, transport-agnostic API for the Magix message bus. It defines the fundamental contracts for communication, including the message structure (`MagixMessage`), endpoint interface (`MagixEndpoint`), and a mechanism for type-safe payload handling (`MagixFormat`).

This API serves as the foundation upon which all other `magix-*` transport implementations (like `magix-rsocket`, `magix-zmq`) are built.

## Key Features

- **`MagixMessage`**: A standardized, serializable data class for all messages, including headers for routing, identification, and security.
- **`MagixEndpoint`**: A universal interface for sending and receiving `MagixMessage`s, abstracting away the underlying transport protocol.
- **`MagixMessageFilter`**: A declarative way to specify which messages a subscriber is interested in.
- **`MagixFormat<T>`**: A type-safe wrapper around `kotlinx.serialization.KSerializer` that links a payload type to a specific format identifier, enabling automatic, polymorphic deserialization.