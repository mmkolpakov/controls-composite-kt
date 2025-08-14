package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.composite.model.*
import space.kscience.controls.composite.model.contracts.AddressSource
import space.kscience.controls.composite.model.contracts.DiscoveredAddressSource
import space.kscience.controls.composite.model.contracts.StaticAddressSource
import space.kscience.controls.composite.model.features.*
import space.kscience.controls.composite.model.messages.*
import space.kscience.controls.composite.model.plans.*

/**
 * A shared [SerializersModule] for the controls-composite models.
 * It provides the necessary polymorphic serialization rules for sealed interfaces
 * like [DeviceMessage], [ActionSpec], [PropertyBinding], and [ChildComponentConfig].
 *
 * This module should be included in any `kotlinx.serialization` `Json` or `Cbor`
 * instance that needs to serialize or deserialize the composite device model.
 */
public val ControlsCompositeSerializersModule: SerializersModule = SerializersModule {

    polymorphic(DeviceMessage::class) {
        subclass(PropertyChangedMessage::class)
        subclass(DescriptionMessage::class)
        subclass(LifecycleStateChangedMessage::class)
        subclass(DeviceErrorMessage::class)
        subclass(BinaryReadyNotification::class)
        subclass(BinaryDataRequest::class)
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

    polymorphic(ActionSpec::class) {
        subclass(AttachActionSpec::class)
        subclass(DetachActionSpec::class)
        subclass(StartActionSpec::class)
        subclass(StopActionSpec::class)
        subclass(WritePropertyActionSpec::class)
        subclass(SequenceActionSpec::class)
        subclass(ParallelActionSpec::class)
    }

    polymorphic(ChildComponentConfig::class) {
        subclass(LocalChildComponentConfig::class)
        subclass(RemoteChildComponentConfig::class)
    }

    polymorphic(AddressSource::class) {
        subclass(StaticAddressSource::class)
        subclass(DiscoveredAddressSource::class)
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
    }
}