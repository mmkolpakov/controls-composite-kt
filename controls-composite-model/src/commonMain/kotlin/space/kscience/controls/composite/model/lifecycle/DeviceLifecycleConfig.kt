package space.kscience.controls.composite.model.lifecycle

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.RestartPolicy
import space.kscience.controls.composite.model.serialization.serializableMetaConverter
import space.kscience.controls.composite.model.serialization.serializableToMeta
import space.kscience.dataforge.meta.*
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
 * Configuration for device state persistence.
 *
 * @property enabled If `true`, enables state persistence for the device.
 * @property mode The [PersistenceMode] to use.
 * @property interval The time interval for saving in `PERIODIC` mode.
 * @property restoreOnStart If `true`, attempts to restore the device's state upon starting.
 */
@Serializable
public data class PersistenceConfig(
    val enabled: Boolean = false,
    val mode: PersistenceMode = PersistenceMode.ON_STOP,
    val interval: Duration = 60.seconds,
    val restoreOnStart: Boolean = true,
) : MetaRepr {
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)
}

/**
 * A comprehensive configuration for a device's lifecycle management within a hub.
 *
 * @property lifecycleMode Defines how the device's lifecycle is tied to its parent.
 * @property startTimeout The maximum duration to wait for the device to start. Null means no timeout.
 * @property stopTimeout The maximum duration to wait for the device to stop. Null means no timeout.
 * @property onError The strategy to apply when this device fails as a child of another device.
 * @property restartPolicy The policy for automatically restarting this device upon failure.
 * @property lazyAttach If true, the device will be instantiated only on first access.
 * @property persistence Configuration for saving and restoring the device's state.
 */
@Serializable
public data class DeviceLifecycleConfig(
    val lifecycleMode: LifecycleMode = LifecycleMode.LINKED,
    val startTimeout: Duration? = 30.seconds,
    val stopTimeout: Duration? = 10.seconds,
    val onError: ChildDeviceErrorHandler = ChildDeviceErrorHandler.RESTART,
    val restartPolicy: RestartPolicy = RestartPolicy(),
    val lazyAttach: Boolean = false,
    val persistence: PersistenceConfig = PersistenceConfig()
) : MetaRepr {
    init {
        startTimeout?.let { require(!it.isNegative()) { "Start timeout must not be negative." } }
        stopTimeout?.let { require(!it.isNegative()) { "Stop timeout must not be negative." } }
    }

    /**
     * Converts this configuration object to a [Meta] representation using the standard serialization mechanism.
     */
    override fun toMeta(): Meta = serializableToMeta(serializer(), this)

    public companion object {
        /**
         * The default, immutable configuration for a device lifecycle.
         */
        public val DEFAULT: DeviceLifecycleConfig = DeviceLifecycleConfig()
    }
}

/**
 * A lazily-initialized [MetaConverter] for [DeviceLifecycleConfig].
 *
 * Usage:
 * ```
 * val meta = MetaConverter.deviceLifecycleConfig.convert(myConfig)
 * val config = MetaConverter.deviceLifecycleConfig.read(myMeta)
 * ```
 */
public val MetaConverter.Companion.deviceLifecycleConfig: MetaConverter<DeviceLifecycleConfig> by lazy {
    serializableMetaConverter(DeviceLifecycleConfig.serializer())
}