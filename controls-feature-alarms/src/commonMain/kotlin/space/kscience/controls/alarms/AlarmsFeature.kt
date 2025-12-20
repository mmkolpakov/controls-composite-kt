package space.kscience.controls.alarms

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta

/**
 * A marker feature indicating that a device is a source of alarms.
 * The runtime uses this feature to activate the alarm management logic for this device,
 * processing its declared `AlarmDescriptor`s.
 */
@Serializable
@SerialName("feature.alarms")
public data object AlarmsFeature : Feature {
    override val capability: String get() = "space.kscience.controls.alarms.AlarmSource"
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}