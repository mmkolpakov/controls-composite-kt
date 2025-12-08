package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.DataType
import space.kscience.controls.composite.model.common.Permission
import space.kscience.controls.composite.model.common.QoS
import space.kscience.controls.composite.model.common.StreamDirection
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.controls.composite.model.specs.policy.MemberPolicies
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * Strategy to handle buffer overflow in streams.
 * This ensures the system behaves predictably under load.
 */
@Serializable
public enum class StreamBackpressure {
    /**
     * Drop the oldest data when buffer is full.
     * Suitable for real-time monitoring (e.g. Video, Sensor Live View) where latency is critical.
     */
    DROP_OLDEST,

    /**
     * Suspend the producer until space is available.
     * Suitable for critical data transfer (e.g. Logs, File transfer) where no data loss is allowed.
     */
    SUSPEND,

    /**
     * Drop the newest incoming data.
     */
    DROP_LATEST
}

/**
 * A serializable, self-contained descriptor for a device's data stream.
 *
 * @property name The unique name of the stream.
 * @property dataType The strict [DataType] of the primary data objects being transmitted.
 * @property protocol A declarative specification of the underlying protocol and framing (e.g., Delimiters).
 * @property backpressure The strategy to use when the consumer is slower than the producer.
 * @property bufferSize The size of the internal buffer for the stream.
 * @property expectedInterval The expected interval between data packets (Heartbeat).
 * @property suggestedRateHz An optional hint suggesting the expected data rate in Hertz.
 * @property direction An optional hint indicating the primary direction of data flow.
 * @property deliveryHint An optional hint suggesting the desired Quality of Service.
 * @property readPermissions Permissions required to read from this stream.
 * @property writePermissions Permissions required to write to this stream.
 * @property tags A set of extensible, semantic tags for classification.
 * @property bindings A map of type-safe, protocol-specific configurations.
 * @property policies A unified specification for all operational policies of this stream.
 */
@Serializable
@DfType("device.stream")
public data class StreamDescriptor(
    override val name: Name,
    public val dataType: DataType,
    public val protocol: ProtocolSpec = ProtocolSpec(),
    public val backpressure: StreamBackpressure = StreamBackpressure.DROP_OLDEST,
    public val bufferSize: Int = 64,
    public val expectedInterval: Duration? = null,
    public val suggestedRateHz: Double? = null,
    public val direction: StreamDirection? = null,
    public val deliveryHint: QoS? = null,
    override val readPermissions: Set<Permission> = emptySet(),
    override val writePermissions: Set<Permission> = emptySet(),
    override val tags: Set<MemberTag> = emptySet(),
    override val bindings: Map<String, AdapterBinding> = emptyMap(),
    override val policies: MemberPolicies = MemberPolicies(),
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "device.stream"
    }
}
