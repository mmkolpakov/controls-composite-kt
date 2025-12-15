package space.kscience.controls.composite.ports

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.Buffer
import okio.ByteString.Companion.toByteString

/**
 * A flow transformation that chunks an incoming [Flow] of [ByteArray]s into messages
 * separated by a given [delimiter]. The delimiter is included at the end of each emitted chunk.
 *
 * @param delimiter The byte sequence to use as a message separator. Must not be empty.
 * @return A new [Flow] that emits complete messages including the delimiter.
 */
public fun Flow<ByteArray>.withDelimiter(delimiter: ByteArray): Flow<ByteArray> {
    require(delimiter.isNotEmpty()) { "Delimiter must not be empty" }
    val delimiterByteString = delimiter.toByteString()

    return flow {
        val buffer = Buffer()
        collect { chunk ->
            buffer.write(chunk)
            var index = buffer.indexOf(delimiterByteString)
            while (index != -1L) {
                // + delimiter.size to include the delimiter itself in the result.
                val messageSize = index + delimiter.size
                emit(buffer.readByteArray(messageSize))
                index = buffer.indexOf(delimiterByteString)
            }
        }
    }
}

/**
 * A flow transformation that chunks an incoming [Flow] of [ByteArray]s into messages of a fixed size.
 * If the last message is incomplete, it will not be emitted.
 *
 * @param size The fixed size of each message. Must be positive.
 * @return A new [Flow] that emits messages of the specified fixed size.
 */
public fun Flow<ByteArray>.withFixedMessageSize(size: Int): Flow<ByteArray> {
    require(size > 0) { "Message size must be positive" }

    return flow {
        val buffer = Buffer()
        collect { chunk ->
            buffer.write(chunk)
            while (buffer.size >= size) {
                emit(buffer.readByteArray(size.toLong()))
            }
        }
    }
}

/**
 * A flow transformation that chunks an incoming [Flow] of [ByteArray]s into messages
 * where each message is prefixed by its length.
 *
 * @param lengthBytes The number of bytes used for the length prefix (1, 2, or 4).
 * @param byteOrder The byte order of the length prefix.
 * @return A new [Flow] that emits complete messages (without the length prefix).
 */
@OptIn(ExperimentalUnsignedTypes::class)
public fun Flow<ByteArray>.withLengthPrefix(
    lengthBytes: Int,
    byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
): Flow<ByteArray> {
    require(lengthBytes in listOf(1, 2, 4)) { "Length prefix must be 1, 2, or 4 bytes." }

    return flow {
        val buffer = Buffer()
        collect { chunk ->
            buffer.write(chunk)
            while (true) {
                if (buffer.size < lengthBytes) break // Not enough data for length prefix

                // Use peek() to read the length without consuming bytes from the buffer.
                // This is crucial for handling cases where the full message hasn't arrived yet.
                val messageLength = when (lengthBytes) {
                    1 -> buffer.peek().readByte().toLong() and 0xFFL
                    2 -> if (byteOrder == ByteOrder.BIG_ENDIAN) {
                        buffer.peek().readShort().toLong() and 0xFFFFL
                    } else {
                        buffer.peek().readShortLe().toLong() and 0xFFFFL
                    }
                    4 -> if (byteOrder == ByteOrder.BIG_ENDIAN) {
                        buffer.peek().readInt().toLong() and 0xFFFFFFFFL
                    } else {
                        buffer.peek().readIntLe().toLong() and 0xFFFFFFFFL
                    }
                    else -> error("Unreachable")
                }

                if (buffer.size < lengthBytes + messageLength) break // Not enough data for the full message

                buffer.skip(lengthBytes.toLong()) // Consume the length prefix from the buffer
                emit(buffer.readByteArray(messageLength)) // Read and emit the message payload
            }
        }
    }
}