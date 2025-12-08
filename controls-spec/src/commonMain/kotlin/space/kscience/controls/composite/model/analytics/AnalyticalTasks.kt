package space.kscience.controls.composite.model.analytics

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.contracts.BlueprintId
import space.kscience.controls.composite.model.contracts.toBlueprintId
import space.kscience.controls.composite.model.meta.requiredAddress
import space.kscience.controls.composite.model.meta.requiredDuration
import space.kscience.controls.composite.model.meta.requiredInstant
import space.kscience.controls.composite.model.meta.requiredName
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.names.Name
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A specification for a task that calculates the average value of a numeric property
 * over a specified time range.
 */
@Serializable(with = AveragePropertyValueTaskSpec.Serializer::class)
public class AveragePropertyValueTaskSpec : Scheme() {
    public var deviceAddress: Address by requiredAddress()
    public var propertyName: Name by requiredName()
    public var startTime: Instant by requiredInstant()
    public var endTime: Instant by requiredInstant()

    public companion object : SchemeSpec<AveragePropertyValueTaskSpec>(::AveragePropertyValueTaskSpec) {
        public val BLUEPRINT_ID: BlueprintId = "controls.task.analytics.averageProperty".toBlueprintId()
    }
    public object Serializer : SchemeAsMetaSerializer<AveragePropertyValueTaskSpec>(Companion)
}

/**
 * A specification for a task that calculates the frequency of an action's invocations.
 */
@Serializable(with = ActionFrequencyTaskSpec.Serializer::class)
public class ActionFrequencyTaskSpec : Scheme() {
    public var deviceAddress: Address by requiredAddress()
    public var actionName: Name by requiredName()
    public var startTime: Instant by requiredInstant()
    public var endTime: Instant by requiredInstant()
    public var window: Duration by requiredDuration()

    public companion object : SchemeSpec<ActionFrequencyTaskSpec>(::ActionFrequencyTaskSpec) {
        /**
         * The unique identifier for the blueprint that executes this task specification.
         */
        public val BLUEPRINT_ID: BlueprintId = "controls.task.analytics.actionFrequency".toBlueprintId()
    }
    public object Serializer : SchemeAsMetaSerializer<ActionFrequencyTaskSpec>(Companion)
}
