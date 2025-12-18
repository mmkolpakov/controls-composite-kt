package space.kscience.controls.composite.old.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name

/**
 * A declarative entry describing a single mirrored property from a remote child device.
 *
 * @property remoteChildName The local name of the remote child proxy (defined via `remoteChild`).
 * @property remotePropertyName The name of the property on the remote device.
 * @property localPropertyName The name under which the mirrored property will be created on the local device.
 * @property valueTypeName The FQN of the property's value type, for runtime validation.
 */
@Serializable
public data class MirrorEntry(
    val remoteChildName: Name,
    val remotePropertyName: Name,
    val localPropertyName: Name,
    val valueTypeName: String,
)

/**
 * A feature that declares a device's capability to mirror properties from remote children.
 * The runtime uses the list of [MirrorEntry] to subscribe to remote property updates and
 * feed them into local, read-only proxy properties.
 */
@Serializable
@SerialName("feature.remoteMirror")
public data class RemoteMirrorFeature(val entries: List<MirrorEntry>) : Feature {
    override val capability: String get() = "space.kscience.controls.composite.old.features.RemoteMirroring"
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}