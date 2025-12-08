package space.kscience.controls.composite.model.contracts.communication

import space.kscience.controls.composite.model.specs.faults.PeerConnectionFault

/**
 * A discriminated union representing the outcome of a peer-to-peer operation.
 * It can either be a [Success] containing the result, or a [Failure] containing a [PeerConnectionFault].
 */
public sealed interface PeerConnectionResult<out T> {
    /**
     * Represents a successful operation.
     * @property value The result of the operation.
     */
    public data class Success<T>(val value: T) : PeerConnectionResult<T>

    /**
     * Represents a failed operation.
     * @property fault A structured object describing the predictable business fault that occurred.
     */
    public data class Failure(val fault: PeerConnectionFault) : PeerConnectionResult<Nothing>
}
