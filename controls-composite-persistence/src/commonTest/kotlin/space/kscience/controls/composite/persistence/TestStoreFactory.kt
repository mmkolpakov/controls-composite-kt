package space.kscience.controls.composite.persistence

/**
 * Creates a platform-specific [SnapshotStore] for testing purposes,
 * executes the given test block, and ensures cleanup of resources afterward.
 *
 * @param block The test logic to be executed with the created store.
 */
expect suspend fun runTestWithStore(block: suspend (store: SnapshotStore) -> Unit)