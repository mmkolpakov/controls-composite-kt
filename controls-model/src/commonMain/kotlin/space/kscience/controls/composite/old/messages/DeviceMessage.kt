package space.kscience.controls.composite.old.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.addressing.Address
import space.kscience.controls.core.identifiers.BlueprintId
import space.kscience.controls.core.identifiers.CorrelationId
import space.kscience.controls.core.messages.DeviceMessage
import space.kscience.controls.core.messages.RequestMessage
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * Notifies that the logical state of a boolean predicate property has changed.
 * This specialized message allows clients to react to changes in conditions (e.g., "ready", "in_range")
 * without needing to subscribe to and evaluate the underlying numeric properties themselves.
 *
 * @param predicateName The name of the predicate property that changed.
 * @param newState The new boolean state of the predicate.
 * @param sourceDevice The address of the device owning the predicate. Mandatory.
 */
@Serializable
@SerialName("predicate.changed")
public data class PredicateChangedMessage(
    override val time: Instant,
    public val predicateName: String,
    public val newState: Boolean,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): PredicateChangedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}


/**
 * Notifies listeners that a new binary content is available for retrieval from this device.
 * The actual data transfer must be done via a separate mechanism like [space.kscience.controls.composite.old.contracts.PeerConnection].
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
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
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
        copy(sourceDevice = sourceDevice?.copy(device = block(sourceDevice.device)))
}

/**
 * A notification that a new device has been attached to a hub.
 * This allows clients to dynamically discover and update the device topology without polling.
 *
 * @param deviceName The local, hierarchical name of the newly attached device.
 * @param blueprintId The identifier of the blueprint used to create the device.
 * @param sourceDevice The address of the hub that originated this message. Mandatory.
 */
@Serializable
@SerialName("hub.deviceAttached")
public data class DeviceAttachedMessage(
    override val time: Instant,
    public val deviceName: Name,
    public val blueprintId: BlueprintId,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): DeviceAttachedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}

/**
 * A notification that a device has been detached from a hub.
 * This allows clients to remove the device from their representation of the hub's topology.
 *
 * @param deviceName The local, hierarchical name of the detached device.
 * @param sourceDevice The address of the hub that originated this message. Mandatory.
 */
@Serializable
@SerialName("hub.deviceDetached")
public data class DeviceDetachedMessage(
    override val time: Instant,
    public val deviceName: Name,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null,
    override val correlationId: CorrelationId? = null,
) : DeviceMessage {
    override fun changeSource(block: (Name) -> Name): DeviceDetachedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}