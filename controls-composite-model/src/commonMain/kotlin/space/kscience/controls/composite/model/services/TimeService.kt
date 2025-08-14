package space.kscience.controls.composite.model.services

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
 * The runtime can provide an implementation that uses NTP or other time synchronization protocols.
 */
public interface TimeService : Plugin {
    override val tag: PluginTag get() = Companion.tag

    /**
     * @return The current, high-precision, synchronized time.
     */
    public fun now(): Instant

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
    override fun now(): Instant = Clock.System.now()
}