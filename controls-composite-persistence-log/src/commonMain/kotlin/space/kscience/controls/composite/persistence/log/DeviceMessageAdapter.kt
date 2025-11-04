package space.kscience.controls.composite.persistence.log

import app.cash.sqldelight.ColumnAdapter
import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.controls.composite.model.serialization.controlsJson

/**
 * A SQLDelight `ColumnAdapter` for the polymorphic `DeviceMessage` interface.
 * It serializes the object to a JSON string for storage in a TEXT column
 * and deserializes it back on retrieval.
 */
internal object DeviceMessageAdapter : ColumnAdapter<DeviceMessage, String> {
    override fun decode(databaseValue: String): DeviceMessage {
        return controlsJson.decodeFromString(DeviceMessage.serializer(), databaseValue)
    }

    override fun encode(value: DeviceMessage): String {
        return controlsJson.encodeToString(DeviceMessage.serializer(), value)
    }
}