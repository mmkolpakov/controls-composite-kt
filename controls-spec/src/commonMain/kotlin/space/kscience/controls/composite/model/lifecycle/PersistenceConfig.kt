package space.kscience.controls.composite.model.lifecycle

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.serialization.duration
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.boolean
import space.kscience.dataforge.meta.enum
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The mode for persisting device state.
 */
@Serializable
public enum class PersistenceMode {
    /** Save the device state only when it is stopped gracefully. */
    ON_STOP,

    /** Save the device state periodically while it is running. */
    PERIODIC
}

/**
 * Configuration for device state persistence, implemented as a [Scheme] for type-safe DSL configuration.
 *
 * @property enabled If `true`, enables state persistence for the device.
 * @property mode The [PersistenceMode] to use.
 * @property interval The time interval for saving in `PERIODIC` mode.
 * @property restoreOnStart If `true`, attempts to restore the device's state upon starting.
 */
@Serializable(with = PersistenceConfig.Serializer::class)
public class PersistenceConfig : Scheme() {
    public var enabled: Boolean by boolean(false)
    public var mode: PersistenceMode by enum(PersistenceMode.ON_STOP)
    public var interval: Duration by duration(60.seconds)
    public var restoreOnStart: Boolean by boolean(true)

    public companion object : SchemeSpec<PersistenceConfig>(::PersistenceConfig)
    public object Serializer : SchemeAsMetaSerializer<PersistenceConfig>(Companion)
}
