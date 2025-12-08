package space.kscience.controls.composite.model.messages

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.alarms.AlarmState
import space.kscience.controls.composite.model.common.Address
import space.kscience.controls.composite.model.common.Principal
import space.kscience.dataforge.names.Name
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A sealed interface for all messages related to the Alarms & Events subsystem.
 * These messages notify clients about changes in the lifecycle of an alarm.
 */
@Serializable
public sealed interface AlarmMessage : DeviceMessage

/**
 * A message notifying that an alarm has transitioned to a new state.
 *
 * @property alarmName The name of the alarm that changed state.
 * @property newState The new state of the alarm.
 * @property oldState The previous state of the alarm, or null if this is the initial transition.
 * @property sourceDevice The address of the device that is the source of the alarm.
 */
@Serializable
public data class AlarmStateChangedMessage(
    override val time: Instant,
    val alarmName: Name,
    val newState: AlarmState,
    val oldState: AlarmState?,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null
) : AlarmMessage {
    override fun changeSource(block: (Name) -> Name): AlarmStateChangedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}

/**
 * A message notifying that an alarm has been acknowledged by a principal.
 *
 * @property alarmName The name of the acknowledged alarm.
 * @property principal The principal who acknowledged the alarm.
 * @property comment An optional comment provided during acknowledgement.
 * @property sourceDevice The address of the device owning the alarm.
 */
@Serializable
public data class AlarmAcknowledgedMessage(
    override val time: Instant,
    val alarmName: Name,
    val principal: Principal,
    val comment: String?,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null
) : AlarmMessage {
    override fun changeSource(block: (Name) -> Name): AlarmAcknowledgedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}

/**
 * A message notifying that an alarm has been temporarily suppressed (shelved).
 *
 * @property alarmName The name of the shelved alarm.
 * @property principal The principal who shelved the alarm.
 * @property duration The duration for which the alarm will be suppressed.
 * @property comment An optional comment.
 * @property sourceDevice The address of the device owning the alarm.
 */
@Serializable
public data class AlarmShelvedMessage(
    override val time: Instant,
    val alarmName: Name,
    val principal: Principal,
    val duration: Duration,
    val comment: String?,
    override val sourceDevice: Address,
    override val targetDevice: Address? = null,
    override val requestId: String? = null
) : AlarmMessage {
    override fun changeSource(block: (Name) -> Name): AlarmShelvedMessage =
        copy(sourceDevice = sourceDevice.copy(device = block(sourceDevice.device)))
}
