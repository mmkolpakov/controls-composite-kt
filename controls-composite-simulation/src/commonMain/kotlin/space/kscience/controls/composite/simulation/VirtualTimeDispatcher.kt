@file:OptIn(InternalCoroutinesApi::class, ExperimentalCoroutinesApi::class)
@file:Suppress("ERROR_SUPPRESSION")

package space.kscience.controls.composite.simulation

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.ThreadSafeHeap
import kotlinx.coroutines.internal.ThreadSafeHeapNode
import kotlinx.coroutines.internal.synchronized
import kotlin.coroutines.CoroutineContext
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * This class exists to allow cleanup code to avoid throwing for cancelled continuations scheduled
 * in the future.
 */
private class CancellableContinuationRunnable(
    val continuation: CancellableContinuation<Unit>,
    private val dispatcher: CoroutineDispatcher
) : Runnable {
    override fun run() = with(dispatcher) { with(continuation) { resumeUndispatched(Unit) } }
}


/**
 * Virtual time manager based on [kotlinx-coroutines-test](https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test/common/src)
 * virtual time manager.
 *
 * This is a scheduler for coroutines used in tests and simulations, providing the delay-skipping behavior.
 *
 * @param parentScope The parent [CoroutineScope] from which this dispatcher inherits its job and context.
 *                    The dispatcher's own lifecycle will be tied to this scope.
 */
public class VirtualTimeDispatcher(
//    parentScope: CoroutineScope
) : CoroutineDispatcher(), Delay, AutoCloseable {

    private val events = ThreadSafeHeap<VirtualTimeDispatchEvent<Any>>()
    private val lock = SynchronizedObject()
    private val count = atomic(0L)

    /**
     * The current virtual time in milliseconds.
     */
    public var currentTime: Long = 0
        get() = synchronized(lock) { field }
        private set

    private val dispatchEvents: Channel<Unit> = Channel(CONFLATED)

    private fun <T : Any> registerEvent(
        timeDeltaMillis: Long,
        marker: T,
        context: CoroutineContext,
    ): DisposableHandle {
        require(timeDeltaMillis >= 0) { "Attempted scheduling an event earlier in time (with the time delta $timeDeltaMillis)" }
        val count = count.getAndIncrement()
        return synchronized(lock) {
            val time = addClamping(currentTime, timeDeltaMillis)
            val event = VirtualTimeDispatchEvent(count, time, marker as Any)
            events.addLast(event)
            dispatchEvents.trySend(Unit)
            DisposableHandle {
                synchronized(lock) {
                    events.remove(event)
                }
            }
        }
    }

//    private fun tryRunNextTaskUnless(condition: () -> Boolean): Boolean {
//        val event = synchronized(lock) {
//            if (condition()) return false
//            val event = events.removeFirstOrNull() ?: return false
//            if (currentTime > event.time)
//                error("The test scheduler entered an invalid state.")
//            currentTime = event.time
//            event
//        }
//        (event.marker as Runnable).run()
//        return true
//    }

    private fun tryRunNextTaskUnless(condition: () -> Boolean): Boolean {
        val event = synchronized(lock) {
            if (condition()) return false
            val event = events.removeFirstOrNull() ?: return false
            if (currentTime > event.time)
                error("The test scheduler entered an invalid state.")
            currentTime = event.time
            event
        }
        (event.marker as Runnable).run()
        return true
    }

    /**
     * Runs the enqueued tasks, advancing the virtual time as needed until there are no more tasks.
     */
    public fun advanceUntilIdle() {
        while (tryRunNextTaskUnless { false }) {
            // Keep running tasks
        }
    }

    /**
     * Runs the tasks that are scheduled to execute at this moment of virtual time.
     */
    public fun runCurrent() {
        val timeMark = synchronized(lock) { currentTime }
        while (true) {
            val event = synchronized(lock) {
                events.removeFirstIf { it.time <= timeMark } ?: return
            }
            (event.marker as Runnable).run()
        }
    }

    /**
     * Moves the virtual clock forward by the specified [delayTime], running scheduled tasks in the meantime.
     */
    public fun advanceTimeBy(delayTime: Duration) {
        require(!delayTime.isNegative()) { "Can not advance time by a negative delay: $delayTime" }
        val startingTime = currentTime
        val targetTime = addClamping(startingTime, delayTime.inWholeMilliseconds)
        while (true) {
            val event = synchronized(lock) {
                val timeMark = currentTime
                val event = events.removeFirstIf { targetTime > it.time }
                when {
                    event == null -> {
                        currentTime = targetTime
                        return
                    }

                    timeMark > event.time -> error("The test scheduler entered an invalid state.")
                    else -> {
                        currentTime = event.time
                        event
                    }
                }
            }
            (event.marker as Runnable).run()
        }
    }

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("VirtualTimeDispatcher.eventLoop")
    )

//    private val eventLoopJob: Job = scope.launch {
//        while (true) {
//            try {
//                val executedSomething = tryRunNextTaskUnless { !isActive }
//                if (executedSomething) {
//                    yield()
//                } else {
//                    receiveDispatchEvent()
//                }
//            } catch (ex: Exception) {
//                if (ex !is CancellationException) {
//                    println("Error in VirtualTimeDispatcher event loop: ${ex.message}")
//                }
//            }
//        }
//    }

//    private val eventLoopJob: Job? = if (autoAdvance) {
//        scope.launch(CoroutineName("Controls virtual time runner")) {
//            while (true) {
//                val executedSomething = tryRunNextTaskUnless { !isActive }
//                if (executedSomething) {
//                    yield()
//                } else {
//                    receiveDispatchEvent()
//                }
//            }
//        }
//    } else {
//        null
//    }

    private suspend fun receiveDispatchEvent() = dispatchEvents.receive()

    /**
     * Returns the [TimeSource] representation of the virtual time of this scheduler.
     */
    public val timeSource: TimeSource.WithComparableMarks = object : AbstractLongTimeSource(DurationUnit.MILLISECONDS) {
        override fun read(): Long = currentTime
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val timedRunnable = CancellableContinuationRunnable(continuation, this)
        val handle = registerEvent(timeMillis, timedRunnable, continuation.context)
        continuation.disposeOnCancellation(handle)
    }

    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle =
        registerEvent(timeMillis, block, context)


    override fun dispatch(context: CoroutineContext, block: Runnable) {
        registerEvent(0, block, context)
    }

    override fun close() {
//        scope.cancel("VirtualTimeDispatcher is closed.")
    }
}

private fun addClamping(a: Long, b: Long): Long = (a + b).let { if (it >= 0) it else Long.MAX_VALUE }

private class VirtualTimeDispatchEvent<T>(
    private val count: Long,
    val time: Long,
    val marker: T,
) : Comparable<VirtualTimeDispatchEvent<*>>, ThreadSafeHeapNode {
    override var heap: ThreadSafeHeap<*>? = null
    override var index: Int = 0

    override fun compareTo(other: VirtualTimeDispatchEvent<*>) =
        compareValuesBy(this, other, VirtualTimeDispatchEvent<*>::time, VirtualTimeDispatchEvent<*>::count)

    override fun toString() = "VirtualTimeDispatchEvent(time=$time)"
}

/**
 * Create a [Clock] based on this scheduler with given time offset for simulation start
 */
public fun VirtualTimeDispatcher.asClock(startTime: Instant = Clock.System.now()): Clock = object : Clock {
    override fun now(): Instant = startTime + currentTime.milliseconds
}