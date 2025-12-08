package space.kscience.controls.composite.model.specs.state

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState
import space.kscience.controls.composite.model.meta.mapOfConvertable
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.ApiVersionSpec
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A specification for a single device within a desired hub state.
 * Implemented as a [Scheme] to allow for type-safe DSL configuration and consistent serialization.
 */
@Serializable(with = DeviceStateDescriptor.Serializer::class)
public class DeviceStateDescriptor : Scheme() {
    /**
     * The ID of the blueprint for this device. This property is mandatory.
     */
    public var blueprintId: BlueprintId by requiredSerializable()

    /**
     * The configuration meta to be applied to this device instance. Defaults to an empty Meta.
     */
    public var configuration: Meta? by convertable(MetaConverter.meta)

    /**
     * The desired lifecycle state for this device. Defaults to [DeviceLifecycleState.Running].
     */
    public var desiredState: DeviceLifecycleState by enum(DeviceLifecycleState.Running)

    public companion object : SchemeSpec<DeviceStateDescriptor>(::DeviceStateDescriptor)
    public object Serializer : SchemeAsMetaSerializer<DeviceStateDescriptor>(Companion)
}

/**
 * A model representing the desired state of an entire `CompositeDeviceHub`.
 * This descriptor is the cornerstone of the GitOps/declarative management pattern.
 */
@Serializable(with = HubStateDescriptor.Serializer::class)
public class HubStateDescriptor : Scheme() {
    /**
     * The version of the HubStateDescriptor schema itself, providing context and enabling
     * version-aware processing by orchestration tools. Defaults are handled by [ApiVersionSpec] itself.
     */
    public var api: ApiVersionSpec by requiredSerializable()

    /**
     * Arbitrary metadata for the descriptor, not directly affecting the hub's configuration. Defaults to an empty Meta.
     */
    public var metadata: Meta? by convertable(MetaConverter.meta)

    /**
     * A map where the key is the local [Name] of a device and the value is its
     * [DeviceStateDescriptor], defining what should be running in the hub. Defaults to an empty map.
     */
    public var devices: Map<Name, DeviceStateDescriptor> by mapOfConvertable(DeviceStateDescriptor.Serializer)

    public companion object : SchemeSpec<HubStateDescriptor>(::HubStateDescriptor)
    public object Serializer : SchemeAsMetaSerializer<HubStateDescriptor>(Companion)
}

/**
 * A lazily-initialized [MetaConverter] for [HubStateDescriptor].
 */
public val MetaConverter.Companion.hubStateDescriptor: MetaConverter<HubStateDescriptor> by lazy {
    HubStateDescriptor
}
