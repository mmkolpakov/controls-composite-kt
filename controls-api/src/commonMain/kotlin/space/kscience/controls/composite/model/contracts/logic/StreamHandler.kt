package space.kscience.controls.composite.model.contracts.logic

import kotlinx.serialization.Serializable
import ru.nsk.kstatemachine.statemachine.StateMachineDslMarker
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.communication.StreamPort
import space.kscience.controls.composite.model.specs.device.StreamDescriptor

/**
 * A behavioral contract for a device's data stream.
 *
 * @param D The type of the device this stream belongs to.
 */
@StateMachineDslMarker
public interface StreamHandler<in D : Device> {
    /**
     * A factory function that creates and returns a [StreamPort] for this data stream.
     * The runtime is responsible for managing the lifecycle of the returned port.
     */
    public val get: suspend D.() -> StreamPort
}


/**
 * An enumeration defining the primary direction of data flow for a stream.
 * This serves as a hint for the runtime and UI generators.
 */
@Serializable
public enum class StreamDirection {
    /** The device primarily sends data through this stream. */
    OUT,

    /** The device primarily receives data through this stream. */
    IN,

    /** The stream is used for significant data transfer in both directions. */
    BIDIRECTIONAL
}
