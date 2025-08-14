package space.kscience.controls.composite.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.Device
import space.kscience.controls.composite.model.contracts.DeviceBlueprint
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleConfig
import space.kscience.controls.composite.model.serialization.serializableToMeta
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
 * Configuration for a child device that exists remotely and is accessed via a proxy.
 *
 * @property address The network-wide [Address] of the remote device.
 * @property peerName The name of the [PeerConnection] blueprint registered in the parent device.
 * @property blueprintId The [DeviceBlueprint] id of the remote device, used for static analysis,
 *                     type safety, and creating the client-side proxy.
 * @property config The [DeviceLifecycleConfig] for managing the lifecycle of the *proxy*.
 * @property meta Additional metadata for configuring the proxy.
 */
@Serializable
@SerialName("remote")
public data class RemoteChildComponentConfig(
    val address: Address,
    val peerName: Name,
    override val blueprintId: BlueprintId,
    override val blueprintVersion: String,
    override val config: DeviceLifecycleConfig,
    override val meta: Meta = Meta.EMPTY,
) : ChildComponentConfig {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}