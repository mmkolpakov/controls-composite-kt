package space.kscience.controls.core

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.core.events.ActionCompleted
import space.kscience.controls.core.events.ActionDispatched
import space.kscience.controls.core.events.ActionStarted
import space.kscience.controls.core.events.CacheHit
import space.kscience.controls.core.events.CacheMiss
import space.kscience.controls.core.events.ExecutionEvent
import space.kscience.controls.core.events.FaultReported
import space.kscience.controls.core.faults.AuthenticationFault
import space.kscience.controls.core.faults.AuthorizationFault
import space.kscience.controls.core.faults.DeviceFault
import space.kscience.controls.core.faults.GenericDeviceFault
import space.kscience.controls.core.faults.InvalidStateFault
import space.kscience.controls.core.faults.NotFoundFault
import space.kscience.controls.core.faults.PreconditionFault
import space.kscience.controls.core.faults.ResourceBusyFault
import space.kscience.controls.core.faults.TimeoutFault
import space.kscience.controls.core.faults.ValidationFault

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
}