package space.kscience.controls.composite.model.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import space.kscience.controls.composite.model.features.*
import space.kscience.controls.composite.model.specs.AddressDown
import space.kscience.controls.composite.model.specs.AddressSource
import space.kscience.controls.composite.model.specs.DiscoveredAddressSource
import space.kscience.controls.composite.model.specs.StaticAddressSource
import space.kscience.controls.composite.model.specs.AddressUp
import space.kscience.controls.composite.model.specs.AddressUpdateEvent
import space.kscience.controls.composite.model.specs.HubDiscoveryEvent
import space.kscience.controls.composite.model.specs.HubDown
import space.kscience.controls.composite.model.specs.HubUp
import space.kscience.controls.composite.model.specs.bindings.ConstPropertyBinding
import space.kscience.controls.composite.model.specs.bindings.PropertyBinding
import space.kscience.controls.composite.model.specs.bindings.SourcePropertyBinding
import space.kscience.controls.composite.model.specs.bindings.TransformedPropertyBinding
import space.kscience.controls.composite.model.specs.device.ChildComponentConfig
import space.kscience.controls.composite.model.specs.device.LocalChildComponentConfig
import space.kscience.controls.composite.model.specs.device.RemoteChildComponentConfig
import space.kscience.controls.composite.model.specs.state.*

/**
 * A [SerializersModule] for the controls-spec models.
 * It includes [coreSerializersModule] and adds layers for Features, Blueprints, and Configs.
 */
public val specSerializersModule: SerializersModule = SerializersModule {
    include(coreSerializersModule)

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
        subclass(AlarmsFeature::class)
        subclass(CustomFeature::class)
    }

    polymorphic(AddressUpdateEvent::class) {
        subclass(AddressUp::class)
        subclass(AddressDown::class)
    }

    polymorphic(HubDiscoveryEvent::class) {
        subclass(HubUp::class)
        subclass(HubDown::class)
    }

    polymorphic(PropertyBinding::class) {
        subclass(ConstPropertyBinding::class)
        subclass(SourcePropertyBinding::class)
        subclass(TransformedPropertyBinding::class)
    }

    polymorphic(StateDiff::class) {
        subclass(DeviceMissing::class)
        subclass(DeviceSuperfluous::class)
        subclass(ConfigDrift::class)
        subclass(LifecycleMismatch::class)
    }
}

/**
 * A shared JSON instance for Spec-level operations.
 */
@OptIn(ExperimentalSerializationApi::class)
public val specJson: Json = Json {
    serializersModule = specSerializersModule
    ignoreUnknownKeys = true
    prettyPrint = true
    classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
}
