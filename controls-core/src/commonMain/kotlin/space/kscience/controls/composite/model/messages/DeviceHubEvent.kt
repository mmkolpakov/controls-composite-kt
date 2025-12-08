package space.kscience.controls.composite.model.messages

import space.kscience.controls.composite.model.common.ExecutionContext

/**
 * A wrapper for a [DeviceMessage] that includes the [ExecutionContext] in which it was generated.
 * This is crucial for preserving cross-cutting concerns like distributed tracing context (`traceContext`)
 * and correlation IDs across asynchronous message boundaries.
 * The `messageFlow` on a [Device] emits instances of this class.
 */
public data class DeviceHubEvent(
    val message: DeviceMessage,
    val context: ExecutionContext
)
