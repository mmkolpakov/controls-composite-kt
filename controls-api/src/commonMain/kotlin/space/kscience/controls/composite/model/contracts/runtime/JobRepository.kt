package space.kscience.controls.composite.model.contracts.runtime

import space.kscience.controls.composite.model.descriptors.JobStatus
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta

/**
 * A persistence layer for long-running asynchronous jobs.
 * This allows the control system to support **Durable Execution**: if the Hub restarts while
 * a long job (e.g., 1-hour calibration) is running, the job's state is preserved and can be
 * resumed or monitored after restart.
 *
 * Implementations typically delegate to a persistent store (Postgres, Redis) or a workflow engine (Temporal.io).
 */
public interface JobRepository : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * Persists the current status of a job.
     * @param jobId The unique identifier of the job.
     * @param status The current status to save.
     */
    public suspend fun saveJobState(jobId: String, status: JobStatus)

    /**
     * Retrieves the last known status of a job.
     * @param jobId The unique identifier of the job.
     * @return The [JobStatus], or null if not found.
     */
    public suspend fun getJobState(jobId: String): JobStatus?

    /**
     * Lists the IDs of all jobs that are currently in an active state (Queued or Running).
     */
    public suspend fun listActiveJobs(): List<String>

    public companion object : PluginFactory<JobRepository> {
        override val tag: PluginTag = PluginTag("device.job.repository", group = PluginTag.DATAFORGE_GROUP)

        override fun build(context: Context, meta: Meta): JobRepository {
            error("JobRepository is a service interface and requires a runtime-specific implementation.")
        }
    }
}
