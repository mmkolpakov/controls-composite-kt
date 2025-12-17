package space.kscience.controls.composite.old.contracts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.nsk.kstatemachine.statemachine.BuildingStateMachine
import space.kscience.controls.composite.old.ChildComponentConfig
import space.kscience.controls.composite.old.DeviceMigrator
import space.kscience.controls.core.InternalControlsApi
import space.kscience.controls.composite.old.contracts.runtime.DeviceFlows
import space.kscience.controls.composite.old.contracts.runtime.HydratableDeviceState
import space.kscience.controls.composite.old.features.Feature
import space.kscience.controls.composite.old.lifecycle.LifecycleContext
import space.kscience.controls.composite.old.meta.*
import space.kscience.controls.core.contracts.Device
import space.kscience.controls.core.identifiers.BlueprintId
import space.kscience.controls.core.descriptors.ActionDescriptor
import space.kscience.controls.core.descriptors.PropertyDescriptor
import space.kscience.controls.core.descriptors.StreamDescriptor
import space.kscience.controls.core.meta.MemberTag
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name

@Serializable
@SerialName("deviceBlueprint")
public data class BlueprintMeta(
    val id: BlueprintId,
    val version: String = "0.1.0",
    val deviceContractFqName: String,
    val features: Map<String, Feature>,
    val peerConnections: Map<Name, PeerBlueprint<out PeerConnection>>,
    val children: Map<Name, ChildComponentConfig>,
    val properties: Collection<PropertyDescriptor>,
    val actions: Collection<ActionDescriptor>,
    val streams: Collection<StreamDescriptor>,
    val meta: Meta,
    val stateMigratorId: String? = null,
    val tags: Set<MemberTag> = emptySet(),
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A complete, self-contained blueprint for a device. This is the central abstraction for defining a device's
 * entire structure, behavior, and lifecycle in a declarative way.
 *
 * A blueprint is a stateless factory that combines:
 * 1.  **Structure**: Child components, properties, actions, and data streams.
 * 2.  **Behavior**: Formal definitions of the device's lifecycle and operational logic as Finite State Machines (FSMs),
 *     as well as the specific implementation logic for properties and actions.
 * 3.  **Instantiation**: A [DeviceDriver] to create the device instance and manage its lifecycle hooks.
 * 4.  **Features**: A structured description of the device's capabilities.
 * 5.  **Versioning**: A static version identifier to support evolution and migration.
 * 6.  **Semantics**: A set of [MemberTag]s to classify the blueprint itself (e.g., as implementing a specific profile).
 *
 * Blueprints are designed to be discoverable via context plugins and are serializable to [Meta] (excluding behavior logic).
 *
 * @param D The type of the device this blueprint creates.
 */
@DfType(DeviceBlueprint.TYPE)
public interface DeviceBlueprint<D : Device> : MetaRepr {
    /**
     * A unique identifier for this blueprint, typically in reverse-DNS format (e.g., "com.example.myDevice").
     * This ID is used by a blueprint registry to discover and resolve blueprints at runtime. It should remain
     * constant across different versions of the same logical blueprint.
     */
    public val id: BlueprintId

    /**
     * A version string for this blueprint, preferably using semantic versioning (e.g., "1.0.2").
     * This allows runtimes to handle different versions of a blueprint, enabling features like state migration
     * and compatibility checks.
     */
    public val version: String get() = "0.1.0"

    /**
     * A set of extensible, semantic [MemberTag]s for classifying the blueprint itself.
     * For example, a blueprint for a Yandex Smart Home light could be tagged with a `ProfileTag("yandex.light.dimmable", "1.0")`.
     */
    public val tags: Set<MemberTag>

    /**
     * A map of features supported by this device. The key is the fully qualified name of the capability interface,
     * and the value is a [Feature] object providing detailed metadata about that capability.
     */
    public val features: Map<String, Feature>

    /**
     * An optional block of code defining the internal reactive logic of the device.
     * This logic is executed by the runtime when the device starts.
     */
    public val logic: (suspend D.(DeviceFlows) -> Unit)?

    /**
     * An optional identifier for a [DeviceMigrator] implementation.
     * The runtime uses this ID to find a migrator during a hot swap operation.
     */
    public val stateMigratorId: String?

    /**
     * A map of peer connection blueprints required by this device to communicate with remote children or other peers.
     * The key is the logical name of the peer connection.
     */
    public val peerConnections: Map<Name, PeerBlueprint<out PeerConnection>>

    /**
     * A map of all child components configured for this device blueprint.
     * The key is the local name of the child.
     */
    public val children: Map<Name, ChildComponentConfig>

    /**
     * A map of all **public** property specifications defined for this device.
     * The key is the property name.
     */
    public val properties: Map<Name, DevicePropertySpec<D, *>>

    /**
     * A map of all **public** action specifications defined for this device.
     * The key is the action name.
     */
    public val actions: Map<Name, DeviceActionSpec<D, *, *>>

    /**
     * A map of all **public** data stream specifications defined for this device.
     * The key is the stream name.
     */
    public val streams: Map<Name, DeviceStreamSpec<D>>

    /**
     * Additional metadata for the blueprint itself. This meta is layered at the bottom
     * of the final device's configuration meta.
     */
    public val meta: Meta

    /**
     * A suspendable DSL block that defines the device's lifecycle as a [ru.nsk.kstatemachine.statemachine.StateMachine].
     * This lambda is applied to a `BuildingStateMachine` instance at runtime when the device is created.
     */
    public val lifecycle: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit

    /**
     * An optional suspendable DSL block that defines the device's operational logic as a separate [ru.nsk.kstatemachine.statemachine.StateMachine].
     * This FSM manages internal states like `IDLE`, `MOVING`, `ACQUIRING`, etc., independently of the lifecycle.
     */
    public val operationalFsm: (suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit)?

    /**
     * The driver responsible for creating the device instance and handling its lifecycle hooks.
     * This driver does NOT contain the logic for properties and actions; that logic is part of the blueprint itself.
     */
    @InternalControlsApi
    public val driver: DeviceDriver<D>

    /**
     * The fully qualified name of the device contract interface 'D'.
     * For runtime validation without full reflection capabilities.
     * The DSL should populate this automatically.
     */
    public val deviceContractFqName: String

    // --- Behavior Logic (non-serializable) ---

    /** A map of suspendable lambdas that define the read logic for properties. */
    @InternalControlsApi
    public val propertyReadLogic: Map<Name, suspend D.() -> Any?>

    /** A map of suspendable lambdas that define the write logic for mutable properties. */
    @InternalControlsApi
    public val propertyWriteLogic: Map<Name, suspend D.(Any?) -> Unit>

    /** A map of suspendable lambdas that define the execution logic for actions. */
    @InternalControlsApi
    public val actionExecutors: Map<Name, suspend D.(Meta?) -> Meta?>

    /** A map of factory functions to create reactive states for derived properties. */
    @InternalControlsApi
    public val derivedStateFactories: Map<Name, HydratableDeviceState<D, *>>

    /**
     * Serializes the declarative parts of the blueprint into a [Meta] object.
     * This representation is suitable for storage, network transmission, and discovery.
     * Non-serializable parts like the driver and behavior logic are omitted.
     */
    override fun toMeta(): Meta = BlueprintMeta(
        id = id,
        version = version,
        deviceContractFqName = deviceContractFqName,
        tags = tags,
        features = features,
        peerConnections = peerConnections,
        children = children,
        properties = properties.values.map { it.descriptor },
        actions = actions.values.map { it.descriptor },
        streams = streams.values.map { it.descriptor },
        meta = meta,
        stateMigratorId = stateMigratorId
    ).toMeta()


    public companion object {
        public const val TYPE: String = "device.blueprint"
    }
}

/**
 * A simple data-holding implementation of [DeviceBlueprint].
 * This class stores all parts of the blueprint, including the non-serializable behavior logic.
 * It also separates the public API members from the non-public ones, which are intended for internal
 * use by the device driver.
 *
 * @property protectedProperties A map of all protected, internal, and private property specifications.
 * @property protectedActions A map of all protected, internal, and private action specifications.
 * @property protectedStreams A map of all protected, internal, and private stream specifications.
 */
@OptIn(InternalControlsApi::class)
public data class SimpleDeviceBlueprint<D : Device>(
    override val id: BlueprintId,
    override val version: String = "0.1.0",
    override val tags: Set<MemberTag> = emptySet(),
    override val features: Map<String, Feature>,
    override val peerConnections: Map<Name, PeerBlueprint<out PeerConnection>>,
    override val children: Map<Name, ChildComponentConfig>,
    override val properties: Map<Name, DevicePropertySpec<D, *>>,
    override val actions: Map<Name, DeviceActionSpec<D, *, *>>,
    override val streams: Map<Name, DeviceStreamSpec<D>>,
    override val meta: Meta,
    @Transient override val lifecycle: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit,
    @Transient override val operationalFsm: (suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit)?,
    @Transient override val driver: DeviceDriver<D>,
    @Transient override val logic: (suspend D.(DeviceFlows) -> Unit)?,
    override val stateMigratorId: String? = null,
    override val deviceContractFqName: String,
    @Transient override val propertyReadLogic: Map<Name, suspend D.() -> Any?>,
    @Transient override val propertyWriteLogic: Map<Name, suspend D.(Any?) -> Unit>,
    @Transient override val actionExecutors: Map<Name, suspend D.(Meta?) -> Meta?>,
    @Transient override val derivedStateFactories: Map<Name, HydratableDeviceState<D, *>>,
    internal val protectedProperties: Map<Name, DevicePropertySpec<D, *>> = emptyMap(),
    internal val protectedActions: Map<Name, DeviceActionSpec<D, *, *>> = emptyMap(),
    internal val protectedStreams: Map<Name, DeviceStreamSpec<D>> = emptyMap(),
) : DeviceBlueprint<D>