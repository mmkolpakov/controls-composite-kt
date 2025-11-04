package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.composite.model.*
import space.kscience.controls.composite.model.contracts.AddressSource
import space.kscience.controls.composite.model.contracts.DiscoveredAddressSource
import space.kscience.controls.composite.model.contracts.StaticAddressSource
import space.kscience.controls.composite.model.features.*
import space.kscience.controls.composite.model.messages.*
import space.kscience.controls.composite.model.meta.AdapterBinding
import space.kscience.controls.composite.model.meta.MemberTag
import space.kscience.controls.composite.model.meta.ProfileTag
import space.kscience.controls.composite.model.plans.*
import space.kscience.controls.composite.model.validation.*

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
        subclass(PredicateChangedMessage::class)
        subclass(BinaryReadyNotification::class)
        subclass(BinaryDataRequest::class)
        subclass(DeviceAttachedMessage::class)
        subclass(DeviceDetachedMessage::class)
    }

    polymorphic(ExecutionEvent::class) {
        subclass(ActionDispatched::class)
        subclass(ActionStarted::class)
        subclass(ActionCompleted::class)
        subclass(CacheHit::class)
        subclass(CacheMiss::class)
        subclass(FaultReported::class)
    }

    polymorphic(DeviceFault::class) {
        subclass(ValidationFault::class)
        subclass(PreconditionFault::class)
        subclass(ResourceBusyFault::class)
        subclass(TimeoutFault::class)
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

    // Registration of serializable validation rules
    polymorphic(ValidationRuleSpec::class) {
        subclass(RangeRuleSpec.serializer())
        subclass(RegexRuleSpec::class)
        subclass(MinLengthRuleSpec::class)
        subclass(CustomPredicateRuleSpec::class)
    }

    polymorphic(MemberTag::class) {
        subclass(ProfileTag::class)
    }

    polymorphic(AdapterBinding::class) {
        // This block is left empty. Subclasses must be registered by modules that define them.
    }
}