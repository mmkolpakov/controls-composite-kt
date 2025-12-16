package space.kscience.controls.core.descriptors

import kotlinx.serialization.Serializable
import space.kscience.controls.core.identifiers.Permission
import space.kscience.controls.core.meta.AdapterBinding
import space.kscience.controls.core.meta.MemberTag
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.controls.core.spec.QoS
import space.kscience.controls.core.spec.StreamDirection
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.misc.DfType
import space.kscience.dataforge.names.Name

/**
 * A serializable, self-contained descriptor for a device's data stream.
 * This object provides all the static information about a stream, making it suitable for
 * introspection and client discovery without needing a live device instance.
 *
 * @property name The unique, potentially hierarchical name of the stream.
 * @property description An optional human-readable description of the stream's purpose.
 * @property dataTypeFqName The fully-qualified class name of the primary data objects or frames
 *                          being transmitted over the stream. This serves as a hint for clients
 *                          on how to decode the raw byte stream.
 * @property permissions The set of permissions required to access (read from or write to) this stream.
 * @property suggestedRateHz An optional hint suggesting the expected data rate in Hertz. UI elements can use
 *                           this for scaling graphs or setting appropriate polling intervals.
 * @property direction An optional hint indicating the primary direction of data flow.
 * @property deliveryHint An optional hint suggesting the desired Quality of Service for the stream's transport.
 *                        The runtime may use this to select or configure the underlying transport mechanism.
 */
@Serializable
@DfType("device.stream")
public data class StreamDescriptor(
    public override val name: Name,
    public val description: String? = null,
    public val dataTypeFqName: String,
    public val permissions: Set<Permission> = emptySet(),
    public val suggestedRateHz: Double? = null,
    public val direction: StreamDirection? = null,
    public val deliveryHint: QoS? = null,
    override val readPermissions: Set<Permission> = permissions,
    override val writePermissions: Set<Permission> = permissions,
    override val tags: Set<MemberTag>,
    override val bindings: Map<String, AdapterBinding>,
) : MemberDescriptor {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        public const val TYPE: String = "device.stream"
    }
}
