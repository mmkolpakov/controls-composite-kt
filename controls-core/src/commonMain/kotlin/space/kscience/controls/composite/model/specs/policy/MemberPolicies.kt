package space.kscience.controls.composite.model.specs.policy

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.DisplaySpec
import space.kscience.controls.composite.model.specs.MetricsSpec
import space.kscience.dataforge.meta.Laminate
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.scheme
import space.kscience.dataforge.meta.schemeOrNull

/**
 * A unified specification for all operational policies of a device member
 * (property, action, or stream). This [Scheme] consolidates cross-cutting concerns like
 * display, execution, state management, and metrics into a single, cohesive block.
 */
@Serializable(with = MemberPolicies.Serializer::class)
public class MemberPolicies : Scheme() {

    /**
     * A composite specification for UI-related metadata.
     * Non-nullable with defaults to encourage proper documentation and grouping.
     */
    public var display: DisplaySpec by scheme(DisplaySpec)

    /**
     * A composite specification for execution policies like timeouts, retries, and resource locking.
     * Nullable: if null, system defaults apply.
     */
    public var execution: ExecutionPolicySpec? by schemeOrNull(ExecutionPolicySpec)

    /**
     * A composite specification for state management policies like persistence and caching.
     * Nullable: if null, system defaults apply.
     */
    public var state: StatePolicySpec? by schemeOrNull(StatePolicySpec)

    /**
     * A composite specification for configuring metrics collection.
     * Nullable: if null, no specific metrics override.
     */
    public var metrics: MetricsSpec? by schemeOrNull(MetricsSpec)

    public companion object : SchemeSpec<MemberPolicies>(::MemberPolicies) {
        /**
         * Merges parent and child policies, serving as an executable contract for runtime implementations.
         * Child policies are layered on top of parent policies, meaning they take precedence. This is achieved
         * by using a [Laminate] of the underlying [space.kscience.dataforge.meta.Meta] objects.
         *
         * @param parent The base policies, typically from a `DeviceBlueprintDeclaration`.
         * @param child The specific policies for a member, which override the parent's.
         * @return A new [MemberPolicies] instance representing the merged result.
         */
        public fun resolve(parent: MemberPolicies, child: MemberPolicies): MemberPolicies {
            val mergedMeta = Laminate(child.meta, parent.meta)
            return MemberPolicies.read(mergedMeta)
        }
    }
    public object Serializer : SchemeAsMetaSerializer<MemberPolicies>(Companion)
}
