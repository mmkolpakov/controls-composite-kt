package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.schemeOrNull

/**
 * A reusable, declarative specification for configuring state management policies
 * like persistence and caching for a device member.
 *
 * This [Scheme] centralizes how the state of properties and actions is handled by the runtime,
 * ensuring consistent behavior and making the blueprint more explicit about its state requirements.
 */
@Serializable(with = StatePolicySpec.Serializer::class)
public class StatePolicySpec : Scheme() {
    /**
     * If `true`, indicates that this element's state should be included in device snapshots
     * and restored on startup if the device's persistence is enabled.
     * This applies to logical properties (`stateProperty`) or any other stateful component.
     * If `null` (default), the decision is left to the runtime or the member's default behavior.
     */
    public var persistent: Boolean? by boolean()

    /**
     * If `true`, explicitly excludes this element's state from device snapshots, overriding
     * any `persistent = true` setting. This is useful for volatile state that should not
     * be restored, such as temporary computation results.
     * If `null` (default), the property is not explicitly marked as transient.
     */
    public var transient: Boolean? by boolean()

    /**
     * An optional policy defining how the results of an idempotent action should be cached.
     * If this policy is defined, the runtime will attempt to cache the action's result
     * according to its parameters (TTL, scope, etc.).
     * If `null`, the action's results will not be cached.
     */
    public var cache: CachePolicy? by schemeOrNull(CachePolicy)

    public companion object : SchemeSpec<StatePolicySpec>(::StatePolicySpec)
    public object Serializer : SchemeAsMetaSerializer<StatePolicySpec>(Companion)
}
