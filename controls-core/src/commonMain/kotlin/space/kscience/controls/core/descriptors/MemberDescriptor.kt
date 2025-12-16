package space.kscience.controls.core.descriptors

import space.kscience.controls.core.identifiers.Permission
import space.kscience.controls.core.meta.AdapterBinding
import space.kscience.controls.core.meta.MemberTag
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name

/**
 * A foundational, sealed interface for all declarative descriptors of device members
 * (properties, actions, and streams).
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

    // Note: Policies are intentionally left out of the core interface for now
    // or should be passed as Meta/specific objects if moved to core.
    // In V2 plan we treat policies as part of the descriptor data class.
}