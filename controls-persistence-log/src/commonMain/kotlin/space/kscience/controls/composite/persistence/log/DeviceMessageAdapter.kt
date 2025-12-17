package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.PolymorphicSerializer
import space.kscience.controls.core.messages.DeviceMessage
import space.kscience.controls.composite.old.serialization.controlsJson

/**
 * A SQLDelight `ColumnAdapter` for the polymorphic `DeviceMessage` interface.
 * It serializes the object to a JSON string for storage in a TEXT column
 * and deserializes it back on retrieval.
 */
internal object DeviceMessageAdapter : ColumnAdapter<DeviceMessage, String> {
    override fun decode(databaseValue: String): DeviceMessage {
        return controlsJson.decodeFromString(PolymorphicSerializer(DeviceMessage::class), databaseValue)
    }

    override fun encode(value: DeviceMessage): String {
        return controlsJson.encodeToString(PolymorphicSerializer(DeviceMessage::class), value)
    }
}