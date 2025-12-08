package space.kscience.controls.composite.model.specs.state

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.descriptors.RedundancyInfo
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState
import space.kscience.controls.composite.model.meta.mapOfConvertable
import space.kscience.controls.composite.model.meta.requiredMeta
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.ApiVersionSpec
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * A serializable descriptor for the actual, observed state of a single device within a hub.
 * This object is a snapshot of a live device's state at a particular moment in time.
 */
@Serializable(with = DeviceActualStateDescriptor.Serializer::class)
public class DeviceActualStateDescriptor : Scheme() {
    /**
     * The ID of the blueprint this device was created from.
     */
    public var blueprintId: String by requiredString()

    /**
     * The current lifecycle state of the device at the time of the snapshot.
     */
    public var lifecycleState: DeviceLifecycleState by enum(DeviceLifecycleState.Detached)

    /**
     * A representation of the final configuration meta that was actually applied to the device instance.
     */
    public var appliedMeta: Meta by requiredMeta()

    public companion object : SchemeSpec<DeviceActualStateDescriptor>(::DeviceActualStateDescriptor)
    public object Serializer : SchemeAsMetaSerializer<DeviceActualStateDescriptor>(Companion)
}

/**
 * A snapshot of the actual state of an entire `CompositeDeviceHub`.
 * This structure provides a complete diagnostic view of the system, including device states
 * and the hub's own operational status (redundancy).
 *
 * This class serves as the Data Transfer Object (DTO) for state logging and system introspection.
 */
@Serializable(with = HubActualStateDescriptor.Serializer::class)
public class HubActualStateDescriptor : Scheme() {
    /**
     * The version of this descriptor's schema.
     */
    public var api: ApiVersionSpec by requiredSerializable()

    /**
     * Arbitrary metadata about the snapshot, such as the timestamp it was taken.
     */
    public var metadata: Meta by requiredMeta()

    /**
     * Information about the redundancy/cluster status of this Hub instance.
     * This allows monitoring tools to visualize the health of the control plane itself.
     */
    public var redundancy: RedundancyInfo by requiredSerializable()

    /**
     * A map of all devices currently present in the hub and their actual states.
     */
    public var devices: Map<Name, DeviceActualStateDescriptor> by mapOfConvertable(DeviceActualStateDescriptor.Serializer)

    public companion object : SchemeSpec<HubActualStateDescriptor>(::HubActualStateDescriptor)
    public object Serializer : SchemeAsMetaSerializer<HubActualStateDescriptor>(Companion)
}
