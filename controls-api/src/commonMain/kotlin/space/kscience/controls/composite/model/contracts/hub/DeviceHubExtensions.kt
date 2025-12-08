package space.kscience.controls.composite.model.contracts.hub

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.contracts.device.Device
import space.kscience.controls.composite.model.contracts.logic.ActionHandler
import space.kscience.controls.composite.model.contracts.logic.MutablePropertyHandler
import space.kscience.controls.composite.model.contracts.logic.PropertyHandler
import space.kscience.controls.composite.model.descriptors.ExecutionResult
import space.kscience.controls.composite.model.descriptors.JobStatus
import space.kscience.controls.composite.model.specs.faults.HubResult
import space.kscience.controls.composite.model.specs.faults.OperationFailure

/**
 * Reads the value of a property from a device specified by its [address] using a type-safe [handler].
 * This is the primary, type-safe way for external clients to read a property via the hub.
 *
 * @param address The network-wide address of the target device.
 * @param handler The [PropertyHandler] defining the property to read, which provides type information.
 * @return A [HubResult] containing the property value of type [T] on success.
 */
public suspend fun <D : Device, T> DeviceHub.read(address: Address, handler: PropertyHandler<D, T>): HubResult<T> {
    return readProperty(address.device, handler.name).mapCatching { meta ->
        handler.converter.read(meta)
    }
}

/**
 * Writes a value to a mutable property on a device specified by its [address] using a type-safe [handler].
 *
 * @param address The network-wide address of the target device.
 * @param handler The [MutablePropertyHandler] defining the property to write.
 * @param value The value of type [T] to write.
 * @return A [HubResult] indicating success or failure.
 */
public suspend fun <D : Device, T> DeviceHub.write(
    address: Address,
    handler: MutablePropertyHandler<D, T>,
    value: T
): HubResult<Unit> {
    return writeProperty(address.device, handler.name, handler.converter.convert(value))
}

/**
 * Executes an action on a device and **waits for its completion**, returning the result.
 *
 * This method handles both [ExecutionResult.Immediate] and [ExecutionResult.Deferred].
 * If the result is deferred (asynchronous), it subscribes to the job status flow and suspends
 * until the job reaches [JobStatus.Completed] or [JobStatus.Failed].
 *
 * @param address The network-wide address of the target device.
 * @param handler The [ActionHandler] defining the action to execute.
 * @param input The input argument for the action.
 * @return A [HubResult] containing the optional result of the action on success.
 * @throws CancellationException if the action was cancelled.
 */
public suspend fun <D : Device, I, O> DeviceHub.executeAndWait(
    address: Address,
    handler: ActionHandler<D, I, O>,
    input: I,
): HubResult<O?> {
    val rawResult = execute(
        address.device,
        handler.name,
        handler.inputConverter.convert(input)
    )

    return rawResult.mapCatching { executionResult ->
        when (executionResult) {
            is ExecutionResult.Immediate -> {
                executionResult.result?.let { handler.outputConverter.read(it) }
            }
            is ExecutionResult.Deferred -> {
                // Find the terminal state
                val terminalStatus = executionResult.statusFlow.first { status ->
                    status is JobStatus.Completed || status is JobStatus.Failed || status is JobStatus.Cancelled
                }

                when (terminalStatus) {
                    is JobStatus.Completed -> terminalStatus.result?.let { handler.outputConverter.read(it) }
                    is JobStatus.Failed -> throw OperationFailure(terminalStatus.failure)
                    is JobStatus.Cancelled -> throw CancellationException("Action '${handler.name}' was cancelled by remote supervisor.")
                    else -> error("Unexpected terminal status: $terminalStatus")
                }
            }
        }
    }
}

/**
 * Executes an action that takes no input ([Unit]) and waits for completion.
 * @see executeAndWait
 */
public suspend fun <D : Device, O> DeviceHub.executeAndWait(
    address: Address,
    handler: ActionHandler<D, Unit, O>
): HubResult<O?> = executeAndWait(address, handler, Unit)

/**
 * Executes an action on a device, but throws an exception if the action is asynchronous/deferred.
 * Use this only when you are certain the action completes immediately (RPC style).
 *
 * @throws UnsupportedOperationException if the action returns a Deferred result.
 */
public suspend fun <D : Device, I, O> DeviceHub.executeSynchronously(
    address: Address,
    handler: ActionHandler<D, I, O>,
    input: I,
): HubResult<O?> {
    return execute(address.device, handler.name, handler.inputConverter.convert(input)).mapCatching { executionResult ->
        when (executionResult) {
            is ExecutionResult.Immediate -> executionResult.result?.let { handler.outputConverter.read(it) }
            is ExecutionResult.Deferred -> throw UnsupportedOperationException(
                "Action '${handler.name}' returned a deferred job '${executionResult.jobId}'. Use 'executeAndWait' instead."
            )
        }
    }
}
