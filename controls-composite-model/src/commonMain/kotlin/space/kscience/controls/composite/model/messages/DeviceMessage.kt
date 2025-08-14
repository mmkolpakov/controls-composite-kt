package space.kscience.controls.composite.model.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.Address
import space.kscience.controls.composite.model.CorrelationId
import space.kscience.controls.composite.model.SerializableDeviceFailure
import space.kscience.controls.composite.model.meta.ActionDescriptor
import space.kscience.controls.composite.model.meta.PropertyDescriptor
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * A sealed interface for all messages that flow through the device system.
 *
 * @property sourceDevice The network-wide address of the device that originated the message. Null for system-generated messages.
 * @property targetDevice The intended recipient of the message. Null for broadcast messages.
 * @property time The timestamp when the message was created.
 * @property requestId A unique identifier for a request, allowing responses to be correlated. Null for notifications.
 * @property correlationId A unique identifier to trace a single logical operation across multiple messages and devices.
 */
@Serializable
public sealed interface DeviceMessage {
    public val sourceDevice: Address?
    public val targetDevice: Address?
    public val time: Instant
    public val requestId: String?
    public val correlationId: CorrelationId?

    /**
     * Creates a copy of a message, modifying the source device's local name by applying a prefix.
     * The hub ID remains unchanged. This is used by composite devices to correctly namespace messages from their children.
     */
    public fun changeSource(block: (Name) -> Name): DeviceMessage
}

/**
 * A message that initiates a request and expects a response.
 */
public interface RequestMessage : DeviceMessage {
    override val requestId: String
}

/**
 * A message that is a response to a [RequestMessage].
 */
public interface ResponseMessage : DeviceMessage {
    override val requestId: String
}

/**
 * Notifies that a property's value has changed. This is a notification, not a response.
 *
 * @param property The name of the property that changed.
 * @param value The new value of the property.
 * @param sourceDevice The name of the device owning the property. Mandatory.
 */
@Serializable
@SerialName("property.changed")
public data class PropertyChangedMessage(
    override val time: Instant,
    public val property: String,
    public val value: Meta,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): PropertyChangedMessage =
        copy(sourceDevice = sourceDevice.copy(deviceName = block(sourceDevice.deviceName)))
}

/**
 * A message providing the full description of a device, including its properties and actions.
 * Typically sent in response to a request.
 *
 * @param description The general metadata of the device.
 * @param properties A collection of descriptors for all supported properties.
 * @param actions A collection of descriptors for all supported actions.
 * @param sourceDevice The name of the described device. Mandatory.
 */
@Serializable
@SerialName("description")
public data class DescriptionMessage(
    override val time: Instant,
    val description: Meta,
    val properties: Collection<PropertyDescriptor>,
    val actions: Collection<ActionDescriptor>,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String,
    override val correlationId: CorrelationId? = null,
) : ResponseMessage {
    override fun changeSource(block: (Name) -> Name): DescriptionMessage =
        copy(sourceDevice = sourceDevice.copy(deviceName = block(sourceDevice.deviceName)))
}

/**
 * Notifies about a change in the device's lifecycle state machine.
 *
 * @param oldStateName The name of the state that was exited. Can be null if this is the initial transition.
 * @param newStateName The name of the state that was entered.
 * @param sourceDevice The name of the device whose state changed. Mandatory.
 */
@Serializable
@SerialName("lifecycle.stateChanged")
public data class LifecycleStateChangedMessage(
    override val time: Instant,
    public val oldStateName: String?,
    public val newStateName: String,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): LifecycleStateChangedMessage =
        copy(sourceDevice = sourceDevice.copy(deviceName = block(sourceDevice.deviceName)))
}

/**
 * A message indicating that an error occurred.
 *
 * @param failure A structured, serializable representation of the error.
 * @param sourceDevice The name of the device where the error occurred. Mandatory.
 */
@Serializable
@SerialName("error")
public data class DeviceErrorMessage(
    override val time: Instant,
    public val failure: SerializableDeviceFailure,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String?,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): DeviceErrorMessage =
        copy(sourceDevice = sourceDevice.copy(deviceName = block(sourceDevice.deviceName)))
}

/**
 * Notifies listeners that a new binary content is available for retrieval from this device.
 * The actual data transfer must be done via a separate mechanism like [space.kscience.controls.composite.model.contracts.PeerConnection].
 *
 * @param contentId A unique identifier for this specific piece of binary content.
 * @param contentMeta Metadata describing the binary content (e.g., size, format).
 * @param sourceDevice The address of the device providing the binary data. Mandatory.
 */
@Serializable
@SerialName("binary.ready")
public data class BinaryReadyNotification(
    override val time: Instant,
    val contentId: String,
    val contentMeta: Meta,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): BinaryReadyNotification =
        copy(sourceDevice = sourceDevice.copy(deviceName = block(sourceDevice.deviceName)))
}

/**
 * A request to retrieve a specific piece of binary content.
 *
 * @param contentId The unique identifier of the content to retrieve.
 * @param sourceDevice The address of the requester.
 * @param targetDevice The address of the device holding the data. Mandatory.
 */
@Serializable
@SerialName("binary.request")
public data class BinaryDataRequest(
    override val time: Instant,
    val contentId: String,
    override val sourceDevice: Address?,
    override val targetDevice: Address,
    override val requestId: String,
    override val correlationId: CorrelationId? = null,
) : RequestMessage {
    override fun changeSource(block: (Name) -> Name): BinaryDataRequest =
        copy(sourceDevice = sourceDevice?.copy(deviceName = block(sourceDevice.deviceName)))
}
