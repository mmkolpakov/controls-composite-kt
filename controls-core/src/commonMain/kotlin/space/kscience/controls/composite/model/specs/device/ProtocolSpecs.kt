package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr

/**
 * A sealed interface describing how a raw byte stream is segmented into discrete messages (frames).
 * This allows the runtime to automatically configure frame decoders (e.g., splitting by newline)
 * without writing custom driver code for every device.
 */
@Serializable
public sealed interface FramingSpec : MetaRepr

/**
 * Specifies that messages are separated by a specific byte sequence (delimiter).
 * Common examples: `\n` (0x0A), `\r\n` (0x0D 0x0A), or ETX (0x03).
 *
 * @property delimiterHex The delimiter sequence represented as a hex string (e.g., "0D0A").
 * @property includeDelimiter If true, the delimiter is included in the resulting message.
 */
@Serializable
@SerialName("framing.delimiter")
public data class DelimiterFraming(
    val delimiterHex: String,
    val includeDelimiter: Boolean = false
) : FramingSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Specifies that messages have a fixed length in bytes.
 *
 * @property length The number of bytes in each message.
 */
@Serializable
@SerialName("framing.fixedLength")
public data class FixedLengthFraming(
    val length: Int
) : FramingSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * Specifies a framing where the length of the message is encoded in a header field.
 *
 * @property lengthFieldOffset The index of the byte where the length field starts.
 * @property lengthFieldLength The size of the length field in bytes (e.g., 1, 2, 4).
 * @property lengthAdjustment An adjustment to add to the value read from the length field to get the total frame length.
 * @property initialBytesToStrip Number of bytes to strip from the beginning of the frame before passing to the logic.
 * @property bigEndian If true, the length field is parsed as Big Endian; otherwise Little Endian.
 */
@Serializable
@SerialName("framing.lengthField")
public data class LengthFieldBasedFraming(
    val lengthFieldOffset: Int = 0,
    val lengthFieldLength: Int = 4,
    val lengthAdjustment: Int = 0,
    val initialBytesToStrip: Int = 0,
    val bigEndian: Boolean = true
) : FramingSpec {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A basic declarative specification for the protocol used on top of the framed stream.
 * This helps the runtime select appropriate default serializers or debuggers.
 */
@Serializable
public enum class ProtocolType {
    /** Raw ASCII text (e.g., SCPI). */
    ASCII,
    /** JSON lines or JSON objects. */
    JSON,
    /** Binary structs. */
    BINARY,
    /** Modbus RTU/TCP frames. */
    MODBUS,
    /** Custom/Proprietary protocol. */
    CUSTOM
}

/**
 * A container for protocol specifications.
 *
 * @property type The high-level type of the protocol.
 * @property framing The rules for splitting the stream into messages.
 * @property charset The character set name (e.g., "UTF-8", "ASCII") if the protocol is text-based.
 */
@Serializable
public data class ProtocolSpec(
    val type: ProtocolType = ProtocolType.CUSTOM,
    val framing: FramingSpec? = null,
    val charset: String = "UTF-8"
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
