package space.kscience.controls.composite.model.specs.state

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * Represents a specific, atomic difference between the Desired State (GitOps/Blueprint)
 * and the Actual State (Runtime).
 *
 * A collection of these diffs is used by the [space.kscience.controls.composite.model.services.Reconciler]
 * to generate a convergence plan.
 */
@Serializable
public sealed interface StateDiff {
    /**
     * The name of the device where the discrepancy was found.
     */
    public val deviceName: Name
}

/**
 * Indicates that a device is present in the Desired State but missing in the Actual State.
 * The Reconciler should typically plan an `attach` operation.
 */
@Serializable
public data class DeviceMissing(
    override val deviceName: Name,
    val blueprintId: BlueprintId,
    val config: Meta
) : StateDiff

/**
 * Indicates that a device exists in the Actual State but is not defined in the Desired State.
 * The Reconciler should typically plan a `detach` operation (orphan collection).
 */
@Serializable
public data class DeviceSuperfluous(
    override val deviceName: Name
) : StateDiff

/**
 * Indicates that the configuration of a device differs between Desired and Actual states.
 * The Reconciler should typically plan a `reconfigure` operation.
 */
@Serializable
public data class ConfigDrift(
    override val deviceName: Name,
    val expected: Meta,
    val actual: Meta
) : StateDiff

/**
 * Indicates that the lifecycle state of the device is not what was expected (e.g., Stopped vs Running).
 * The Reconciler should typically plan a `start` or `stop` operation.
 */
@Serializable
public data class LifecycleMismatch(
    override val deviceName: Name,
    val expectedState: String,
    val actualState: String
) : StateDiff
