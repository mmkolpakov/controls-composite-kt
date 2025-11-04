package space.kscience.controls.composite.persistence

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

/**
 * A serializer for `ByteArray` that encodes to and decodes from a Base64 string using Okio.
 */
internal object Base64ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlin.ByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.toByteString().base64())
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return decoder.decodeString().decodeBase64()?.toByteArray()
            ?: throw IllegalArgumentException("Invalid base64 string")
    }
}