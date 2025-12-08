package space.kscience.controls.composite.model.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.data.RawValue
import space.kscience.controls.composite.model.state.DataQuality
import space.kscience.dataforge.names.Name
import kotlin.time.Instant

/**
 * A single update for a specific tag/property within a telemetry packet.
 * To reduce network overhead in high-frequency streams,
 * the [property] name can be omitted if [alias] is provided. The client and server must
 * negotiate aliases beforehand via [negotiateAliases].
 *
 * @property property The name of the property being updated. Nullable if [alias] is used.
 * @property alias An integer identifier for the property, negotiated for the session.
 * @property value The raw value of the property, optimized for zero-overhead transmission.
 * @property quality The quality status of this specific value.
 * @property timestamp The origin timestamp of this specific value.
 */
@Serializable
public data class TagUpdate(
    val property: Name? = null,
    val alias: Int? = null,
    val value: RawValue,
    val quality: DataQuality,
    val timestamp: Instant
) {
    init {
        require(property != null || alias != null) { "TagUpdate must contain either a property name or an alias." }
    }
}

/**
 * A high-performance packet containing multiple property updates.
 * This message type bypasses the heavy `Meta` structures and uses `RawValue` for the Data Plane.
 *
 * @property sourceDevice The address of the device originating the telemetry.
 * @property updates The list of updates contained in this packet.
 * @property time The timestamp of the packet creation (server time).
 * @property targetDevice The destination, if applicable.
 * @property requestId The correlation ID, if this is a response to a specific request.
 */
@Serializable
@SerialName("telemetry")
public data class TelemetryPacket(
    override val sourceDevice: Address,
    val updates: List<TagUpdate>,
    override val time: Instant,
    override val targetDevice: Address? = null,
    override val requestId: String? = null
) : DeviceMessage {

    override fun changeSource(block: (Name) -> Name): TelemetryPacket =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}
