package space.kscience.controls.composite.model.descriptors

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.faults.SerializableDeviceFailure
import space.kscience.dataforge.meta.Meta

/**
 * Represents the dynamic status of a long-running asynchronous job.
 */
@Serializable
public sealed interface JobStatus {
    /** The job has been accepted and is queued for execution. */
    @Serializable
    public data object Queued : JobStatus

    /** The job is currently running. */
    @Serializable
    public data class Running(val progress: Double? = null, val message: String? = null) : JobStatus

    /** The job completed successfully. */
    @Serializable
    public data class Completed(val result: Meta?) : JobStatus

    /** The job failed. */
    @Serializable
    public data class Failed(val failure: SerializableDeviceFailure) : JobStatus

    /** The job was cancelled by a user or system. */
    @Serializable
    public data object Cancelled : JobStatus
}

/**
 * The result of an execution request sent to a device.
 * It distinguishes between operations that complete immediately and those that are deferred (asynchronous).
 */
public sealed interface ExecutionResult {
    /**
     * The operation completed immediately (synchronously).
     * This is typical for simple set-point writes or fast commands (RPC style).
     *
     * @property result The optional return value of the operation.
     */
    public data class Immediate(val result: Meta?) : ExecutionResult

    /**
     * The operation has been accepted but will take time to complete.
     * This matches the "Task" pattern in industrial systems (e.g., calibration, complex movement).
     *
     * @property jobId The unique identifier of the background job.
     * @property statusFlow A hot flow of status updates for this specific job.
     */
    public data class Deferred(
        val jobId: String,
        val statusFlow: Flow<JobStatus>
    ) : ExecutionResult
}
