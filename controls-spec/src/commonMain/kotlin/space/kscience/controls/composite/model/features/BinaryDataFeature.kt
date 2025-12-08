package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A feature indicating that the device supports direct transfer of large binary data,
 * bypassing the standard message bus for efficiency. This is an analogue to `PeerConnection` from `controls-kt`.
 *
 * @property formats A list of supported binary content types or formats (e.g., "image/jpeg", "custom-binary-format").
 */
@Serializable
@SerialName("feature.binaryData")
public data class BinaryDataFeature(
    val formats: List<String> = emptyList()
) : Feature {
    override val capability: String = "space.kscience.controls.composite.model.contracts.PeerConnection"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
