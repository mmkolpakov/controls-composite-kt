package space.kscience.controls.composite.simulation

import space.kscience.dataforge.context.ContextBuilder
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.names.asName
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Configures the context to use a virtual timeline, starting at the given [start] time.
 * All `delay` calls and `Clock` access within this context will be managed by a `VirtualTimeDispatcher`.
 * This is the primary entry point for setting up a deterministic simulation environment.
 *
 * @param start The initial [Instant] for the virtual timeline. Defaults to the current system time.
 */
public fun ContextBuilder.withVirtualTime(start: Instant = Clock.System.now()) {
    plugin(ClockManager) {
        set("clock".asName(), Meta {
            "mode" put "virtual"
            "start" put start.toString()
        })
    }
}

/**
 * Configures the context to use a compressed or expanded timescale.
 * `delay` calls will be scaled by the given [compression] factor. For example, a compression
 * factor of `10.0` will make `delay(1.seconds)` complete in 100 milliseconds of real time.
 *
 * @param compression The time compression factor. Values > 1.0 speed up time; values < 1.0 slow it down.
 * @throws IllegalArgumentException if compression is not positive.
 */
public fun ContextBuilder.withTimeCompression(compression: Double) {
    require(compression > 0.0) { "Time compression must be a positive number." }
    plugin(ClockManager) {
        set("clock".asName(), Meta {
            "mode" put "compressed"
            "compression" to compression
        })
    }
}