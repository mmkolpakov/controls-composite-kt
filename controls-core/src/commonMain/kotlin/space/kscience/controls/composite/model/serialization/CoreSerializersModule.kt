package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.composite.model.common.*
import space.kscience.controls.composite.model.messages.*
import space.kscience.controls.composite.model.meta.*
import space.kscience.controls.composite.model.specs.bindings.*
import space.kscience.controls.composite.model.specs.device.*
import space.kscience.controls.composite.model.specs.faults.*
import space.kscience.controls.composite.model.specs.plans.*
import space.kscience.controls.composite.model.specs.reference.*
import space.kscience.controls.composite.model.specs.transformer.*
import space.kscience.controls.composite.model.validation.CustomPredicateRuleDescriptor
import space.kscience.controls.composite.model.validation.MinLengthRuleDescriptor
import space.kscience.controls.composite.model.validation.RangeRuleDescriptor
import space.kscience.controls.composite.model.validation.RegexRuleDescriptor
import space.kscience.controls.composite.model.validation.ValidationRuleDescriptor

/**
 * A shared [SerializersModule] for the controls-core models.
 * It provides the necessary polymorphic serialization rules for sealed interfaces defined in Core.
 */
public val coreSerializersModule: SerializersModule = SerializersModule {
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
        subclass(ActionFaultMessage::class)
        subclass(AlarmStateChangedMessage::class)
        subclass(AlarmAcknowledgedMessage::class)
        subclass(AlarmShelvedMessage::class)
        subclass(TelemetryPacket::class)
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
        subclass(NotFoundFault::class)
        subclass(AuthenticationFault::class)
        subclass(AuthorizationFault::class)
        subclass(InvalidStateFault::class)
        subclass(GenericDeviceFault::class)
    }

    polymorphic(ComputableValue::class) {
        subclass(LiteralValue::class)
        subclass(ReferenceValue::class)
    }

    polymorphic(PropertyTransformerDescriptor::class) {
        subclass(ToStringTransformerDescriptor::class)
        subclass(LinearTransformDescriptor::class)
        subclass(PolynomialTransformerDescriptor::class)
        subclass(LookupTableTransformerDescriptor::class)
        subclass(ChainTransformerDescriptor::class)
        subclass(CustomTransformerDescriptor::class)
    }

    polymorphic(ReferenceTarget::class) {
        subclass(DevicePropertyTarget::class)
        subclass(ActionOutputTarget::class)
        subclass(EnvironmentTarget::class)
    }

    polymorphic(ReferenceTransform::class) {
        subclass(JsonPathTransform::class)
        subclass(ToStringTransform::class)
        subclass(AverageTransform::class)
    }

    polymorphic(PlanActionSpec::class) {
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
        subclass(LoopActionSpec::class)
        subclass(RunWorkspaceTaskSpec::class)
        subclass(ConditionalActionSpec::class)
        subclass(AwaitSignalActionSpec::class)
        subclass(DriverActionSpec::class)
        subclass(AcknowledgeAlarmActionSpec::class)
        subclass(ShelveAlarmActionSpec::class)
        subclass(DeclareVariableActionSpec::class)
        subclass(SetVariableActionSpec::class)
    }

    polymorphic(MemberDescriptor::class) {
        subclass(PropertyDescriptor::class)
        subclass(ActionDescriptor::class)
        subclass(StreamDescriptor::class)
        subclass(SignalDescriptor::class)
        subclass(AlarmDescriptor::class)
    }

    polymorphic(MemberTag::class) {
        subclass(ProfileTag::class)
        subclass(AliasTag::class)
    }

    polymorphic(AdapterBinding::class) {
        subclass(ModbusTestBinding::class)
        subclass(CustomAdapterBinding::class)
    }

    polymorphic(FramingSpec::class) {
        subclass(DelimiterFraming::class)
        subclass(FixedLengthFraming::class)
        subclass(LengthFieldBasedFraming::class)
    }

    polymorphic(TransformationSpec::class) {
        subclass(LinearTransformation::class)
        subclass(ExpressionTransformation::class)
        subclass(MapTransformation::class)
        subclass(MovingAverageTransformation::class)
        subclass(IntegrationTransformation::class)
    }

    polymorphic(ActionLogicSource::class) {
        subclass(ExternalLogic::class)
        subclass(PlanLogic::class)
        subclass(TaskLogic::class)
    }

    polymorphic(PeerConnectionFault::class) {
        subclass(ConnectionFailed::class)
        subclass(Timeout::class)
        subclass(ContentNotFound::class)
        subclass(CommunicationError::class)
    }

    // Validation rules are in Core because PropertyDescriptor uses them
    polymorphic(ValidationRuleDescriptor::class) {
        subclass(RangeRuleDescriptor::class)
        subclass(RegexRuleDescriptor::class)
        subclass(MinLengthRuleDescriptor::class)
        subclass(CustomPredicateRuleDescriptor::class)
    }
}
