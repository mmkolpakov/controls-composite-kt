package space.kscience.controls.composite.old

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.identifiers.BlueprintId
import space.kscience.controls.core.contracts.DeviceBlueprint
import space.kscience.controls.composite.old.lifecycle.DeviceLifecycleConfig
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name

/**
 * Represents the configuration for a child device within a composite device.
 * This is a sealed interface to allow for different types of children, like local and remote.
 */
@Serializable
public sealed interface ChildComponentConfig : MetaRepr {
    public val blueprintId: BlueprintId
    public val blueprintVersion: String
    public val config: DeviceLifecycleConfig
    public val meta: Meta
}

/**
 * Configuration for a child device that is instantiated locally within the same hub.
 *
 * @property blueprintId The [DeviceBlueprint] id that defines the child's structure and logic.
 * @property config The [DeviceLifecycleConfig] for managing the child's lifecycle.
 * @property meta Additional metadata to be passed to the child device upon instantiation.
 * @property bindings A list of declarative property bindings for this child.
 */
@Serializable
@SerialName("local")
public data class LocalChildComponentConfig(
    override val blueprintId: BlueprintId,
    override val blueprintVersion: String,
    override val config: DeviceLifecycleConfig,
    override val meta: Meta = Meta.EMPTY,
    val bindings: ChildPropertyBindings = ChildPropertyBindings(emptyList()),
) : ChildComponentConfig {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Configuration for a child device that exists remotely and is accessed via a local proxy.
 * This class contains all the necessary static information for the runtime to create and manage
 * the proxy and its connection to the remote device.
 *
 * The full network-wide [Address] of the remote device is constructed at runtime by combining
 * the logical `hubId` resolved from the `peerName` connection with the provided `remoteDeviceName`.
 *
 * @property remoteDeviceName The local, potentially hierarchical, name of the target device *on the remote hub*.
 *                            This is NOT the name of the local proxy.
 * @property peerName The local name of the [PeerBlueprint] registered in the parent device's specification.
 *                    The runtime uses this name to look up the connection details (like host, port, and protocol)
 *                    and to resolve the `hubId` of the remote hub.
 * @property blueprintId The [DeviceBlueprint] id of the remote device.
 * @property blueprintVersion The version of the remote device's blueprint, used for compatibility checks.
 * @property config The [DeviceLifecycleConfig] for managing the lifecycle of the *local proxy*, not the remote device itself.
 *                  For example, it defines restart policies for the connection to the remote device.
 * @property meta Additional metadata for configuring the local proxy instance.
 */
@Serializable
@SerialName("remote")
public data class RemoteChildComponentConfig(
    val remoteDeviceName: Name,
    val peerName: Name,
    override val blueprintId: BlueprintId,
    override val blueprintVersion: String,
    override val config: DeviceLifecycleConfig,
    override val meta: Meta = Meta.EMPTY,
) : ChildComponentConfig {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}