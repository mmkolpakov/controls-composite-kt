package space.kscience.controls.fsm

import kotlinx.serialization.Serializable

/**
 * Defines the lifecycle relationship between a parent and a child device.
 */
@Serializable
public enum class LifecycleMode {
    /**
     * The child's lifecycle is linked to the parent's. Starting the parent starts the child,
     * and stopping the parent stops the child.
     */
    LINKED,

    /**
     * The child's lifecycle is independent. It must be started and stopped manually.
     * The parent does not control its lifecycle.
     */
    INDEPENDENT
}

/**
 * Defines how and when a device should be started when it's attached to a hub.
 */
@Serializable
public enum class StartMode {
    /**
     * Do not start the device upon attachment. It must be started manually later.
     */
    NONE,

    /**
     * Start the device asynchronously. The attachment process does not wait for the device to become fully active.
     */
    ASYNC,

    /**
     * Start the device synchronously. The attachment call blocks until the device is fully active or fails to start.
     */
    SYNC
}

/**
 * Defines the strategy for handling errors that occur in a child device.
 */
@Serializable
public enum class ChildDeviceErrorHandler {
    /**
     * Ignore the error. The child device will transition to an ERROR state, but no further action is taken.
     */
    IGNORE,

    /**
     * Attempt to restart the child device according to its `RestartPolicy`.
     */
    RESTART,

    /**
     * Stop the parent device if a critical error occurs in the child.
     */
    STOP_PARENT,

    /**
     * Propagate the exception up, causing the parent's operation to fail.
     */
    PROPAGATE
}