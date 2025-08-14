package space.kscience.controls.composite.model.contracts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.nsk.kstatemachine.statemachine.BuildingStateMachine
import space.kscience.controls.composite.model.ChildComponentConfig
import space.kscience.controls.composite.model.DeviceMigrator
import space.kscience.controls.composite.model.contracts.runtime.DeviceFlows
import space.kscience.controls.composite.model.features.Feature
import space.kscience.controls.composite.model.lifecycle.LifecycleContext
import space.kscience.controls.composite.model.meta.ActionDescriptor
import space.kscience.controls.composite.model.meta.DeviceActionSpec
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.controls.composite.model.meta.PropertyDescriptor
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name

@Serializable
@SerialName("deviceBlueprint")
public data class BlueprintMeta(
    val id: BlueprintId,
    val version: String = "0.1.0",
    val features: Map<String, Feature>,
    val peerConnections: Map<Name, PeerBlueprint<out PeerConnection>>,
    val children: Map<Name, ChildComponentConfig>,
    val properties: Collection<PropertyDescriptor>,
    val actions: Collection<ActionDescriptor>,
    val meta: Meta,
    val stateMigratorId: String? = null,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A complete, self-contained blueprint for a device. This is the central abstraction for defining a device's
 * entire structure, behavior, and lifecycle in a declarative way.
 *
 * A blueprint is a stateless factory that combines:
 * 1.  **Structure**: Child components, properties, and actions.
 * 2.  **Behavior**: Formal definitions of the device's lifecycle and operational logic as Finite State Machines (FSMs).
 * 3.  **Instantiation**: A [DeviceDriver] to create the device instance.
 * 4.  **Features**: A structured description of the device's capabilities.
 * 5.  **Versioning**: A static version identifier to support evolution and migration.
 *
 * Blueprints are designed to be discoverable via context plugins and are serializable to [Meta].
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
     * A map of all property specifications defined for this device.
     * The key is the property name.
     */
    public val properties: Map<Name, DevicePropertySpec<D, *>>

    /**
     * A map of all action specifications defined for this device.
     * The key is the action name.
     */
    public val actions: Map<Name, DeviceActionSpec<D, *, *>>

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
     * The driver responsible for creating the device instance and handling its physical interactions.
     */
    public val driver: DeviceDriver<D>

    /**
     * Serializes the declarative parts of the blueprint into a [Meta] object.
     * This representation is suitable for storage, network transmission, and discovery.
     * Non-serializable parts like the driver and logic lambdas are omitted.
     */
    override fun toMeta(): Meta = BlueprintMeta(
        id = id,
        version = version,
        features = features,
        peerConnections = peerConnections,
        children = children,
        properties = properties.values.map { it.descriptor },
        actions = actions.values.map { it.descriptor },
        meta = meta,
        stateMigratorId = stateMigratorId
    ).toMeta()


    public companion object {
        public const val TYPE: String = "device.blueprint"
    }
}

/**
 * A simple data-holding implementation of [DeviceBlueprint].
 */
public data class SimpleDeviceBlueprint<D : Device>(
    override val id: BlueprintId,
    override val version: String = "0.1.0",
    override val features: Map<String, Feature>,
    override val peerConnections: Map<Name, PeerBlueprint<out PeerConnection>>,
    override val children: Map<Name, ChildComponentConfig>,
    override val properties: Map<Name, DevicePropertySpec<D, *>>,
    override val actions: Map<Name, DeviceActionSpec<D, *, *>>,
    override val meta: Meta,
    override val lifecycle: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit,
    override val operationalFsm: (suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit)?,
    override val driver: DeviceDriver<D>,
    override val logic: (suspend D.(DeviceFlows) -> Unit)?,
    override val stateMigratorId: String? = null,
) : DeviceBlueprint<D>