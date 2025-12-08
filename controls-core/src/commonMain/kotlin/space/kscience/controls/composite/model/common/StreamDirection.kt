package space.kscience.controls.composite.model.common

import kotlinx.serialization.Serializable

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
