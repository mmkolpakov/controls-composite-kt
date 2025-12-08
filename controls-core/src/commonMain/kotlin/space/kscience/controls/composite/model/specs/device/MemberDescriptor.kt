package space.kscience.controls.composite.model.specs.device

import space.kscience.controls.composite.model.common.Permission
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name

/**
 * A foundational, sealed interface for all declarative descriptors of device members
 * (properties, actions, and streams).
 *
 * This interface centralizes all operational policies (display, execution, state, metrics)
 * into a single, non-nullable [policies] property. This simplifies the contract and enables
 * a clear inheritance model where members can receive default policies from their parent blueprint.
 *
 * V0.3 Update: Permissions are now split into [readPermissions] and [writePermissions] for
 * granular Role-Based Access Control (RBAC).
 */
public sealed interface MemberDescriptor : MetaRepr {
    /**
     * The unique, potentially hierarchical name of the device member.
     */
    public val name: Name

    /**
     * A collection of permissions required to *read* the state, receive telemetry, or view
     * the metadata of this member.
     */
    public val readPermissions: Set<Permission>

    /**
     * A collection of permissions required to *write* to this property, *execute* this action,
     * or *send* data to this stream.
     */
    public val writePermissions: Set<Permission>

    /**
     * A set of extensible, semantic tags for classification by external systems.
     */
    public val tags: Set<MemberTag>

    /**
     * A map of type-safe, protocol-specific configurations.
     */
    public val bindings: Map<String, AdapterBinding>

    /**
     * A unified specification for all operational policies of this member.
     * The runtime is expected to apply these policies by layering them on top of the
     * blueprint-level `defaultPolicies`. This property is non-nullable; if no specific
     * policies are defined for a member, it should hold an empty `MemberPolicies` instance.
     */
    public val policies: MemberPolicies
}
