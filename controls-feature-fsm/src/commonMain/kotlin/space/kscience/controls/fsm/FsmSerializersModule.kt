package space.kscience.controls.fsm

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.messages.DeviceMessage

public val fsmSerializersModule: SerializersModule = SerializersModule {
    polymorphic(Feature::class) {
        subclass(LifecycleFeature::class)
        subclass(OperationalFsmFeature::class)
    }

    polymorphic(DeviceMessage::class) {
        subclass(LifecycleStateChangedMessage::class)
    }
}