package space.kscience.controls.composite.persistence

import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * Base exception for snapshot store operations.
 */
public open class SnapshotStoreException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Thrown when a storage quota is exceeded (e.g., in browser localStorage).
 */
public class SnapshotQuotaException(message: String, cause: Throwable? = null) : SnapshotStoreException(message, cause)

/**
 * Thrown when stored snapshot data is corrupted or cannot be parsed.
 */
public class SnapshotFormatException(message: String, cause: Throwable? = null) : SnapshotStoreException(message, cause)


/**
 * An interface for a storage system that can save and load device state snapshots.
 * Snapshots are represented as [Meta] objects and identified by the device's [Name].
 */
public interface SnapshotStore {
    /**
     * Saves a snapshot for a given device name.
     * If a snapshot for this name already exists, it should be overwritten.
     *
     * @param name The unique name of the device.
     * @param snapshot The [Meta] object representing the device's state.
     * @throws SnapshotStoreException on general storage failures.
     * @throws SnapshotQuotaException if storage limits are exceeded.
     */
    public suspend fun save(name: Name, snapshot: Meta)

    /**
     * Loads the latest snapshot for a given device name.
     *
     * @param name The unique name of the device.
     * @return The saved [Meta] snapshot, or `null` if no snapshot is found.
     * @throws SnapshotFormatException if the stored data is malformed.
     * @throws SnapshotStoreException on general storage failures.
     */
    public suspend fun load(name: Name): Meta?

    /**
     * Deletes the snapshot for a given device name.
     * Does nothing if no snapshot is found.
     *
     * @param name The unique name of the device.
     * @throws SnapshotStoreException on general storage failures.
     */
    public suspend fun delete(name: Name)
}