package space.kscience.controls.composite.old.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.alarms.alarmsSerializersModule
import space.kscience.controls.automation.automationSerializersModule
import space.kscience.controls.composite.old.*
import space.kscience.controls.composite.old.contracts.AddressSource
import space.kscience.controls.composite.old.contracts.DiscoveredAddressSource
import space.kscience.controls.composite.old.contracts.StaticAddressSource
import space.kscience.controls.composite.old.features.*
import space.kscience.controls.composite.old.messages.*
import space.kscience.controls.core.controlsCoreSerializersModule
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.messages.DeviceMessage
import space.kscience.controls.fsm.fsmSerializersModule

/**
 * A shared [SerializersModule] for the controls-composite models.
 * It provides the necessary polymorphic serialization rules for sealed interfaces
 * like [space.kscience.controls.core.messages.DeviceMessage], [space.kscience.controls.automation.ActionSpec], [PropertyBinding], and [ChildComponentConfig].
 *
 * This module should be included in any `kotlinx.serialization` `Json` or `Cbor`
 * instance that needs to serialize or deserialize the composite device old.
 */
public val ControlsCompositeSerializersModule: SerializersModule = SerializersModule {

    include(controlsCoreSerializersModule)
    include(automationSerializersModule)
    include(fsmSerializersModule)
    include(alarmsSerializersModule)

    polymorphic(DeviceMessage::class) {
        subclass(PredicateChangedMessage::class)
        subclass(BinaryReadyNotification::class)
        subclass(BinaryDataRequest::class)
        subclass(DeviceAttachedMessage::class)
        subclass(DeviceDetachedMessage::class)
    }

    polymorphic(PropertyBinding::class) {
        subclass(ConstPropertyBinding::class)
        subclass(ParentPropertyBinding::class)
        subclass(TransformedPropertyBinding::class)
    }

    polymorphic(PropertyTransformerDescriptor::class) {
        subclass(ToStringTransformerDescriptor::class)
        subclass(LinearTransformDescriptor::class)
    }

    polymorphic(ChildComponentConfig::class) {
        subclass(LocalChildComponentConfig::class)
        subclass(RemoteChildComponentConfig::class)
    }

    polymorphic(AddressSource::class) {
        subclass(StaticAddressSource::class)
        subclass(DiscoveredAddressSource::class)
    }

    polymorphic(GuardSpec::class) {
        subclass(TimedPredicateGuardSpec::class)
        subclass(ValueChangeGuardSpec::class)
    }

    polymorphic(Feature::class) {
        subclass(ReconfigurableFeature::class)
        subclass(StatefulFeature::class)
        subclass(DataSourceFeature::class)
        subclass(TaskExecutorFeature::class)
        subclass(BinaryDataFeature::class)
        subclass(IntrospectionFeature::class)
        subclass(RemoteMirrorFeature::class)
        subclass(OperationalGuardsFeature::class)
    }
}