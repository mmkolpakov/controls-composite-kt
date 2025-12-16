package space.kscience.controls.core

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.core.descriptors.*
import space.kscience.controls.core.events.*
import space.kscience.controls.core.faults.*
import space.kscience.controls.core.meta.*
import space.kscience.controls.core.validation.*

/**
 * A shared [SerializersModule] for the controls-core models.
 * It provides the necessary polymorphic serialization rules for sealed interfaces defined in Core.
 */
public val controlsCoreSerializersModule: SerializersModule = SerializersModule {
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

    polymorphic(MemberTag::class) {
        subclass(ProfileTag::class)
        subclass(AliasTag::class)
    }

    polymorphic(AdapterBinding::class) {
        subclass(ModbusTestBinding::class)
    }

    polymorphic(ValidationRuleDescriptor::class) {
        subclass(RangeRuleDescriptor::class)
        subclass(RegexRuleDescriptor::class)
        subclass(MinLengthRuleDescriptor::class)
        subclass(CustomPredicateRuleDescriptor::class)
    }

    polymorphic(ActionLogicSource::class) {
        subclass(ExternalLogic::class)
    }

    polymorphic(MemberDescriptor::class) {
        subclass(PropertyDescriptor::class)
        subclass(ActionDescriptor::class)
        subclass(StreamDescriptor::class)
    }
}