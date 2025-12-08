package space.kscience.controls.composite.model.specs.device

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.specs.bindings.PropertyBinding
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleConfig
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredMeta
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * Represents the configuration for a child device within a composite device.
 * This is a sealed interface to allow for different types of children, like local and remote.
 */
@Serializable
public sealed interface ChildComponentConfig : MetaRepr {
    public val blueprintId: BlueprintId
    public val blueprintVersion: String
    public val config: DeviceLifecycleConfig
    public val metaConfig: Meta
}

/**
 * Configuration for a child device that is instantiated locally within the same hub.
 */
@SerialName("local")
@Serializable(with = LocalChildComponentConfig.Serializer::class)
public class LocalChildComponentConfig : Scheme(), ChildComponentConfig {
    override var blueprintId: BlueprintId by requiredSerializable()
    override var blueprintVersion: String by requiredString()
    override var config: DeviceLifecycleConfig by scheme(DeviceLifecycleConfig)
    override var metaConfig: Meta by requiredMeta()
    public var bindings: List<PropertyBinding> by listOfConvertable(PolymorphicSerializer(PropertyBinding::class))

    public companion object : SchemeSpec<LocalChildComponentConfig>(::LocalChildComponentConfig)
    public object Serializer : SchemeAsMetaSerializer<LocalChildComponentConfig>(Companion)
}

/**
 * Configuration for a child device that exists remotely and is accessed via a local proxy.
 */
@SerialName("remote")
@Serializable(with = RemoteChildComponentConfig.Serializer::class)
public class RemoteChildComponentConfig : Scheme(), ChildComponentConfig {
    public var remoteDeviceName: Name by requiredName()
    public var peerName: Name by requiredName()
    override var blueprintId: BlueprintId by requiredSerializable()
    override var blueprintVersion: String by requiredString()
    override var config: DeviceLifecycleConfig by scheme(DeviceLifecycleConfig)
    override var metaConfig: Meta by requiredMeta()

    public companion object : SchemeSpec<RemoteChildComponentConfig>(::RemoteChildComponentConfig)
    public object Serializer : SchemeAsMetaSerializer<RemoteChildComponentConfig>(Companion)
}
