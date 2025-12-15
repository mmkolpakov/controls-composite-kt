# Module controls-persistence

**Maturity**: EXPERIMENTAL

## Description

This module provides the API and multiplatform implementations for device state persistence. It allows the runtime to save snapshots of a device's state and restore them later, which is crucial for resilience against restarts and for stateful services.

## Key Features

- **`SnapshotStore` Interface**: A simple, asynchronous contract for saving, loading, and deleting device state snapshots, which are represented as `Meta` objects.
- **Platform-Specific Implementations**:
    - **JVM & Native**: `FileSnapshotStore` saves snapshots to the local filesystem using `okio`.
    - **JS & WasmJs**: `LocalStorageSnapshotStore` saves snapshots to the browser's `localStorage`.
    - **Common**: `InMemorySnapshotStore` provides a non-persistent, in-memory store, primarily for testing.
- **`PersistencePlugin`**: A DataForge plugin that manages and provides `SnapshotStore` instances based on configuration, making the persistence layer pluggable.