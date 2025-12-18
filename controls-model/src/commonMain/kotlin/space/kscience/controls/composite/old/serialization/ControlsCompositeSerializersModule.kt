package space.kscience.controls.composite.old.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.composite.old.*
import space.kscience.controls.composite.old.contracts.AddressSource
import space.kscience.controls.composite.old.contracts.DiscoveredAddressSource
import space.kscience.controls.composite.old.contracts.StaticAddressSource
import space.kscience.controls.composite.old.features.*
import space.kscience.controls.composite.old.messages.*
import space.kscience.controls.composite.old.plans.*
import space.kscience.controls.core.controlsCoreSerializersModule
import space.kscience.controls.core.features.Feature
import space.kscience.controls.core.messages.DescriptionMessage
import space.kscience.controls.core.messages.DeviceErrorMessage
import space.kscience.controls.core.messages.DeviceMessage
import space.kscience.controls.core.messages.PropertyChangedMessage

/**
 * A shared [SerializersModule] for the controls-composite models.
 * It provides the necessary polymorphic serialization rules for sealed interfaces
 * like [space.kscience.controls.core.messages.DeviceMessage], [ActionSpec], [PropertyBinding], and [ChildComponentConfig].
 *
 * This module should be included in any `kotlinx.serialization` `Json` or `Cbor`
 * instance that needs to serialize or deserialize the composite device old.
 */
public val ControlsCompositeSerializersModule: SerializersModule = SerializersModule {

    polymorphic(DeviceMessage::class) {
        subclass(LifecycleStateChangedMessage::class)
        subclass(PredicateChangedMessage::class)
        subclass(BinaryReadyNotification::class)
        subclass(BinaryDataRequest::class)
        subclass(DeviceAttachedMessage::class)
        subclass(DeviceDetachedMessage::class)
    }

    include(controlsCoreSerializersModule)

    polymorphic(PropertyBinding::class) {
        subclass(ConstPropertyBinding::class)
        subclass(ParentPropertyBinding::class)
        subclass(TransformedPropertyBinding::class)
    }

    polymorphic(PropertyTransformerDescriptor::class) {
        subclass(ToStringTransformerDescriptor::class)
        subclass(LinearTransformDescriptor::class)
    }

    polymorphic(ActionSpec::class) {
        subclass(AttachActionSpec::class)
        subclass(DetachActionSpec::class)
        subclass(StartActionSpec::class)
        subclass(StopActionSpec::class)
        subclass(WritePropertyActionSpec::class)
        subclass(SequenceActionSpec::class)
        subclass(ParallelActionSpec::class)
        subclass(DelayActionSpec::class)
        subclass(AwaitPredicateActionSpec::class)
        subclass(InvokeActionSpec::class)
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
        subclass(LifecycleFeature::class)
        subclass(ReconfigurableFeature::class)
        subclass(StatefulFeature::class)
        subclass(DataSourceFeature::class)
        subclass(TaskExecutorFeature::class)
        subclass(OperationalFsmFeature::class)
        subclass(BinaryDataFeature::class)
        subclass(PlanExecutorFeature::class)
        subclass(IntrospectionFeature::class)
        subclass(RemoteMirrorFeature::class)
        subclass(OperationalGuardsFeature::class)
    }
}