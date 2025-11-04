package space.kscience.controls.composite.model.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A standard [MemberTag] for formally declaring that a [space.kscience.controls.composite.model.contracts.DeviceBlueprint]
 * implements a specific, named profile or conforms to a "dialect". This is the primary mechanism for high-level
 * classification of devices.
 *
 * For example, a blueprint for a dimmable light that is compatible with a hypothetical "Yandex Smart Home" adapter
 * could be tagged with `ProfileTag("yandex.light.dimmable", "1.0")`. External systems and validation tools can
 * then use this tag to verify compatibility and apply appropriate logic.
 *
 * @property name A unique, dot-separated name for the profile (e.g., "yandex.light.dimmable", "modbus.temperatureSensor").
 * @property version A version string for the profile, preferably using semantic versioning.
 */
@Serializable
@SerialName("tag.profile")
public data class ProfileTag(val name: String, val version: String) : MemberTag