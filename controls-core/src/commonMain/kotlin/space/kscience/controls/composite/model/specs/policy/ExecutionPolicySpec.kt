package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.ResourceLockSpec
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.schemeOrNull

/**
 * A reusable, declarative specification for defining execution policies related to resilience
 * and concurrency control for a device member (action or property).
 *
 * This [Scheme] centralizes the configuration for timeouts, retries, and resource locking,
 * providing a consistent way to apply these policies.
 */
@Serializable(with = ExecutionPolicySpec.Serializer::class)
public class ExecutionPolicySpec : Scheme() {
    /**
     * An optional policy for a recommended timeout on a single execution of a logic block.
     * The runtime should enforce this to prevent operations from hanging indefinitely.
     * If not specified, a system-wide default may be used.
     */
    public var timeout: TimeoutPolicy? by schemeOrNull(TimeoutPolicy)

    /**
     * An optional policy defining a hard deadline for the total duration of an operation,
     * including any queue time, retries, and other delays. This provides a global backstop
     * to ensure an operation terminates within a predictable timeframe.
     */
    public var deadline: TimeoutPolicy? by schemeOrNull(TimeoutPolicy)

    /**
     * An optional policy for automatically retrying a failed operation.
     * If not specified, operations will not be retried by default.
     */
    public var retry: RetryPolicy? by schemeOrNull(RetryPolicy)

    /**
     * A list of resource locks that must be acquired by the runtime *before* the operation
     * is executed. This is the primary mechanism for preventing race conditions and ensuring
     * safe concurrent access to shared physical or logical resources (e.g., a serial port, a file).
     */
    public var requiredLocks: List<ResourceLockSpec> by listOfConvertable(ResourceLockSpec.serializer())

    public companion object : SchemeSpec<ExecutionPolicySpec>(::ExecutionPolicySpec)
    public object Serializer : SchemeAsMetaSerializer<ExecutionPolicySpec>(Companion)
}
