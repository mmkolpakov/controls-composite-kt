package space.kscience.controls.telemetry

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.core.messages.DeviceMessage

public val telemetrySerializersModule: SerializersModule = SerializersModule {
    polymorphic(DeviceMessage::class) {
        subclass(TelemetryPacket::class)
    }
}