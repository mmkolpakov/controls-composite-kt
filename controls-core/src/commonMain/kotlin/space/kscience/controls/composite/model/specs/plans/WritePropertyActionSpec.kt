package space.kscience.controls.composite.model.specs.plans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.LocalizedText
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.meta.requiredConvertable
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.meta.requiredSerializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.serializable
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.controls.composite.model.specs.reference.ComputableValue
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name

/**
 * An action to write a value to a device property.
 *
 * @property deviceAddress The network address of the target device.
 * @property propertyName The name of the property to write.
 * @property value The value to write, represented as a [ComputableValue]. The runtime is responsible
 *                 for resolving this value before executing the write operation.
 */
@SerialName("write")
@Serializable(with = WritePropertyActionSpec.Serializer::class)
public class WritePropertyActionSpec : Scheme(), PlanActionSpec {
    public var deviceAddress: Address by requiredAddress()
    public var propertyName: Name by requiredName()
    public var value: ComputableValue by requiredSerializable()

    override var policies: ActionPoliciesSpec by scheme(ActionPoliciesSpec)

    /**
     * Optional description for this write step.
     */
    override var description: LocalizedText? by convertable(MetaConverter.serializable(LocalizedText.serializer()))

    public companion object : SchemeSpec<WritePropertyActionSpec>(::WritePropertyActionSpec)
    public object Serializer : SchemeAsMetaSerializer<WritePropertyActionSpec>(Companion)
}
