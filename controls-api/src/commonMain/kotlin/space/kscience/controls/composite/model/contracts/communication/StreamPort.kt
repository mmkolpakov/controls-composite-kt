package space.kscience.controls.composite.model.contracts.communication

import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer
import space.kscience.controls.composite.model.specs.device.StreamDescriptor

/**
 * A port for continuous, low-latency, bidirectional streaming of binary data.
 * Unlike `Port`, which is designed for discrete byte array messages, `StreamPort` is optimized for
 * scenarios involving continuous data flow, such as video feeds, high-frequency sensor data,
 * or waveform transmission.
 *
 * A `StreamPort` is a managed resource. The runtime is responsible for its entire lifecycle.
 */
public interface StreamPort : AutoCloseable {
    /**
     * The descriptor defining the configuration of this port.
     * Implementations MUST respect the policies defined here, specifically `backpressure` and `bufferSize`.
     */
    public val descriptor: StreamDescriptor

    /**
     * A hot [Flow] of incoming data chunks.
     *
     * Consumers of this flow are responsible for reassembling messages if necessary.
     * The implementation handles backpressure according to [descriptor].
     */
    public val incoming: Flow<Buffer>

    /**
     * Sends a chunk of binary data through the port.
     *
     * @param buffer The [Buffer] containing the data to be sent.
     */
    public suspend fun send(buffer: Buffer)
}
