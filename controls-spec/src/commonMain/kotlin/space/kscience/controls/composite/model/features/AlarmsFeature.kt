package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A marker feature indicating that a device is a source of alarms.
 * The runtime uses this feature to activate the alarm management logic for this device,
 * processing its declared `AlarmSpec`s and monitoring the associated predicate properties.
 */
@Serializable
@SerialName("feature.alarms")
public data object AlarmsFeature : Feature {
    override val capability: String get() = "space.kscience.controls.composite.model.alarms.AlarmSource"
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}
