package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A feature indicating that the device can expose its properties as a `dataforge-data` `DataSource`.
 * This enables seamless integration with the DataForge data processing and analysis ecosystem.
 * The runtime is responsible for providing the actual `DataSource` implementation based on this feature.
 *
 * @property dataTypeString The common upper bound type for all data items produced by this source, represented as a string.
 *                          The actual `KType` is not serializable and must be handled by the runtime.
 */
@Serializable
@SerialName("feature.dataSource")
public data class DataSourceFeature(
    val dataTypeString: String?,
) : Feature {
    override val capability: String get() = "space.kscience.dataforge.data.DataSource"

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
