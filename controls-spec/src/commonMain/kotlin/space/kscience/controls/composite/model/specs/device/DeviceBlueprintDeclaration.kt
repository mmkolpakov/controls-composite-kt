package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.features.Feature
import space.kscience.controls.composite.model.lifecycle.LifecyclePlans
import space.kscience.controls.composite.model.meta.*
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.PeerBlueprintDeclaration
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A pure, serializable, and platform-agnostic declaration of a device blueprint's structure and contract.
 *
 * This class is implemented as a [Scheme], making it a type-safe, declarative "view" over a DataForge [Meta] object.
 * This design allows for powerful features like native inheritance via `Laminate` and flexible construction using a DSL.
 * It contains all static information required for introspection, validation, UI generation, and storage,
 * but intentionally excludes any executable logic like drivers or behavior lambdas. This strict separation of
 * declaration from implementation is a core principle that ensures blueprints are portable and can be materialized
 * on different platforms by their respective runtimes.
 *
 * It serves as the primary artifact for GitOps-style device management and is the target for importers that parse
 * external configuration formats.
 */
@Serializable(with = DeviceBlueprintDeclaration.Serializer::class)
@SerialName("device.blueprint.declaration")
public class DeviceBlueprintDeclaration : Scheme() {
    /**
     * The unique identifier for this blueprint.
     */
    public var id: BlueprintId by requiredSerializable()

    /**
     * The semantic version of this blueprint (e.g., "1.2.3").
     * Defaults to "0.0.0" if not specified.
     */
    public var version: String by string { "0.0.0" }

    /**
     * A unique identifier for the specific logic implementation to be used with this blueprint.
     * This allows multiple implementations (e.g., hardware, simulation) for the same blueprint contract.
     * Defaults to "default".
     */
    public var logicId: String by string { "default" }

    /**
     * A monotonically increasing integer version for the device's persistent state schema.
     * Defaults to 1.
     */
    public var schemaVersion: Int by int(1)

    /**
     * The fully qualified name of the device contract interface.
     * Defaults to the base [space.kscience.controls.composite.model.contracts.device.Device] contract.
     */
    public var deviceContractFqName: String by string { "space.kscience.controls.composite.model.contracts.device.Device" }

    /**
     * An optional reference to a parent blueprint. If provided, this blueprint inherits all
     * properties, actions, children, and other members from the parent. Members with the same
     * name in this child blueprint will override those from the parent. This enables creating
     * specialized device types from more general ones.
     */
    public var inheritsFrom: BlueprintId? by serializable()

    /**
     * A set of default operational policies that will be inherited by all properties and
     * actions in this blueprint. Member-specific policies will be layered on top of these defaults.
     * This mechanism significantly reduces boilerplate in member declarations.
     */
    public var defaultPolicies: MemberPolicies by scheme(MemberPolicies)

    /**
     * A map of capabilities supported by this device.
     */
    public var features: Map<Name, Feature> by mapOfConvertable(PolymorphicSerializer(Feature::class))

    /**
     * A map of peer connection blueprints required for communication.
     */
    public var peerConnections: Map<Name, PeerBlueprintDeclaration> by mapOfConvertable(PeerBlueprintDeclaration.serializer())

    /**
     * A map of child component configurations.
     */
    public var children: Map<Name, ChildComponentConfig> by mapOfConvertable(PolymorphicSerializer(ChildComponentConfig::class))

    /**
     * A collection of all public property descriptors defined in this blueprint.
     */
    public var properties: List<PropertyDescriptor> by listOfSerializable()

    /**
     * A collection of all public action descriptors defined in this blueprint.
     */
    public var actions: List<ActionDescriptor> by listOfSerializable()

    /**
     * A collection of all alarm specifications defined in this blueprint.
     */
    public var alarms: List<AlarmDescriptor> by listOfSerializable()

    /**
     * A collection of all public data stream descriptors.
     */
    public var streams: List<StreamDescriptor> by listOfSerializable()

    /**
     * A declarative definition of the device's lifecycle logic using transaction plans.
     * Mandatory, as lifecycle is critical.
     */
    public var lifecycle: LifecyclePlans by requiredSerializable()

    /**
     * Additional static metadata for the blueprint.
     */
    public var metaData: Meta? by convertable(MetaConverter.meta)

    /**
     * An optional identifier for a state migration logic. The runtime uses this ID to find
     * a [space.kscience.controls.composite.model.services.StateMigrator] in the
     * [space.kscience.controls.composite.model.services.StateMigratorRegistry] during a `hotSwap`
     * operation if the old device's `schemaVersion` is less than the new one's.
     */
    public var stateMigratorId: String? by string()

    /**
     * A set of semantic tags for classifying the blueprint itself.
     */
    public var tags: Set<MemberTag> by setOfSerializable()

    public companion object : SchemeSpec<DeviceBlueprintDeclaration>(::DeviceBlueprintDeclaration)
    public object Serializer : SchemeAsMetaSerializer<DeviceBlueprintDeclaration>(Companion)
}
