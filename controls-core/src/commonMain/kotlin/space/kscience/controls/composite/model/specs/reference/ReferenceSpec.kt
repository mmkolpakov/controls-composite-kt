package space.kscience.controls.composite.model.specs.reference

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.meta.listOfConvertable
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * An interface representing the source of a referenced value.
 * This interface is polymorphic to allow for different types of sources (properties, action outputs, etc.).
 */
@Polymorphic
public interface ReferenceTarget

/**
 * A reference to a device property.
 * @property address The full network-wide address of the target device.
 * @property propertyName The name of the property on the target device.
 */
@Serializable
@SerialName("property")
public data class DevicePropertyTarget(val address: Address, val propertyName: Name) : ReferenceTarget

/**
 * A reference to the output of a previously executed action within a transaction plan.
 * @property actionKey The idempotency key of the action whose output is being referenced.
 * @property outputRef The hierarchical path to the specific value within the action's output [Meta].
 *                     If the path is empty, it refers to the entire output meta.
 */
@Serializable
@SerialName("actionOutput")
public data class ActionOutputTarget(val actionKey: String, val outputRef: Name) : ReferenceTarget

/**
 * A reference to an environment variable or a static value within the execution context.
 * @property name The name of the environment variable.
 */
@Serializable
@SerialName("env")
public data class EnvironmentTarget(val name: Name) : ReferenceTarget

/**
 * An interface representing a transformation to be applied to a resolved value.
 * This interface is polymorphic to allow for a variety of standard and custom transformations.
 */
@Polymorphic
public interface ReferenceTransform

/**
 * A transformation that extracts a value from a [Meta] object using a JSONPath expression.
 * @property path The JSONPath expression to apply.
 */
@Serializable
@SerialName("transform.jsonPath")
public data class JsonPathTransform(val path: String) : ReferenceTransform

/**
 * A transformation that converts the resolved value to its string representation.
 */
@Serializable
@SerialName("transform.toString")
public data object ToStringTransform : ReferenceTransform

/**
 * A transformation that computes the average of a numeric property over a historical window.
 * This requires a runtime that can query historical data (e.g., from an `AuditLogService`).
 * @property window The duration of the time window to consider for the average.
 */
@Serializable
@SerialName("transform.average")
public data class AverageTransform(val window: Duration) : ReferenceTransform


/**
 * A [Scheme] for building a reference to a dynamic value.
 * This is the primary mechanism for serializing and deserializing references via the DataForge Meta system.
 * It combines a [target] (the source of the value) with an ordered list of [transforms].
 */
@Serializable(with = ReferenceSpec.Serializer::class)
public class ReferenceSpec : Scheme() {
    /**
     * The source of the value to be referenced. This is a mandatory, polymorphic property.
     */
    public var target: ReferenceTarget by requiredConvertable(
        MetaConverter.serializable(PolymorphicSerializer(ReferenceTarget::class))
    )

    /**
     * A list of transformations to be applied sequentially to the resolved value.
     */
    public var transforms: List<ReferenceTransform> by listOfConvertable(PolymorphicSerializer(ReferenceTransform::class))

    public companion object : SchemeSpec<ReferenceSpec>(::ReferenceSpec)
    public object Serializer : SchemeAsMetaSerializer<ReferenceSpec>(ReferenceSpec)
}
