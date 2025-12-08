package space.kscience.controls.composite.model.features

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.Name
import kotlin.time.Duration

/**
 * A serializable specification for a single "guard".
 * A guard monitors a predicate property and, when its condition is met for a specified duration,
 * posts an event to the device's operational FSM.
 */
@Serializable
public sealed interface GuardSpec

/**
 * A guard that triggers after a boolean predicate remains true for a given duration.
 *
 * @property predicateName The name of the boolean property (a predicate) to monitor.
 * @property holdFor The duration for which the predicate must remain `true` before the event is posted.
 * @property postEventSerialName The fully qualified serial name of the event class to be posted.
 * @property eventMeta Optional metadata to be included when constructing the event instance.
 * @property onlyInStates An optional set of state names. If provided, the guard is only active when the
 *                        operational FSM is in one of these states.
 */
@Serializable
@SerialName("guard.timedPredicate")
public data class TimedPredicateGuardSpec(
    val predicateName: Name,
    val holdFor: Duration,
    val postEventSerialName: String,
    val eventMeta: Meta = Meta.EMPTY,
    val onlyInStates: Set<String> = emptySet()
) : GuardSpec

/**
 * A feature that declares a device's use of operational guards.
 * The runtime uses this feature to set up the necessary monitoring and FSM event posting logic.
 */
@Serializable
@SerialName("feature.operationalGuards")
public data class OperationalGuardsFeature(val guards: List<GuardSpec>) : Feature {
    override val capability: String get() = CAPABILITY

    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        /**
         * The unique, fully-qualified name for the OperationalGuards capability.
         */
        public const val CAPABILITY: String = "space.kscience.controls.composite.model.features.Guards"
    }
}
