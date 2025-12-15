package space.kscience.controls.composite.old.services

import space.kscience.dataforge.context.AbstractPlugin
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.context.PluginFactory
import space.kscience.dataforge.context.PluginTag
import space.kscience.dataforge.meta.Meta
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A contract for a service that provides synchronized time.
 * In a distributed SCADA system, it is crucial that all components share a common
 * understanding of time for correct event ordering and data timestamping.
 *
 * The runtime can provide an implementation that uses NTP or other time synchronization protocols,
 * or a virtual clock for simulations.
 */
public interface TimeService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * The [Clock] instance provided by this service.
     */
    public val clock: Clock

    /**
     * @return The current, high-precision, synchronized time. A shortcut for `clock.now()`.
     */
    public fun now(): Instant = clock.now()

    public companion object : PluginFactory<TimeService> {
        override val tag: PluginTag = PluginTag("device.time", group = PluginTag.DATAFORGE_GROUP)

        /**
         * Builds a default [TimeService] that uses the system clock.
         * Suitable for single-node deployments or testing.
         */
        override fun build(context: Context, meta: Meta): TimeService = SystemTimeService(meta)
    }
}

/**
 * A default implementation of [TimeService] that relies on [Clock.System].
 */
private class SystemTimeService(meta: Meta) : AbstractPlugin(meta), TimeService {
    override val tag: PluginTag get() = TimeService.tag
    override val clock: Clock = Clock.System
}