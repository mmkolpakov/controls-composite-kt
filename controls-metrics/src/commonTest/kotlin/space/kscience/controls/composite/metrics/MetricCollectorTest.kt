package space.kscience.controls.composite.metrics

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class MetricCollectorTest {

    @Test
    fun testCounter() = runTest {
        val collector = AtomicMetricCollector("test.collector")
        val counter = collector.counter(MetricId("test.counter"))
        assertEquals(0L, counter.value)

        counter.inc()
        assertEquals(1L, counter.value)

        counter.inc(5)
        assertEquals(6L, counter.value)
    }

    @Test
    fun testGauge() = runTest {
        val collector = AtomicMetricCollector("test.collector")
        val gauge = collector.gauge(MetricId("test.gauge"), initialValue = 10.0)
        assertEquals(10.0, gauge.value)

        gauge.value = -5.5
        assertEquals(-5.5, gauge.value)
    }

    @Test
    fun testConcurrentCounter() = runTest {
        val collector = AtomicMetricCollector("test.concurrent")
        val counter = collector.counter(MetricId("counter.inc"))
        val numCoroutines = 100
        val incrementsPerCoroutine = 1000

        coroutineScope {
            val jobs = List(numCoroutines) {
                launch {
                    repeat(incrementsPerCoroutine) {
                        counter.inc()
                    }
                }
            }
            jobs.forEach { it.join() }
        }

        val expected = (numCoroutines * incrementsPerCoroutine).toLong()
        assertEquals(expected, counter.value, "Concurrent increments on Counter should be thread-safe")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTimedBlock() = runTest {
        val collector = AtomicMetricCollector("test.timed")
        // `timed` function requires a Histogram, not a Gauge.
        val histogram = collector.histogram(MetricId("op.duration"), buckets = listOf(0.1, 0.2))

        val result = timed(histogram) {
            delay(100) // Advances virtual time
            "OK"
        }

        assertEquals("OK", result)
        val startTime = currentTime
        val timedResult = timed(histogram) {
            delay(100.milliseconds)
            "Done"
        }
        val endTime = currentTime
        assertEquals("Done", timedResult)
        assertEquals(100L, endTime - startTime, "Virtual time should advance by 100ms")
        assertTrue(histogram.sum >= 0, "Histogram sum should be non-negative.")
        assertEquals(2, histogram.value) // two observations
    }

    @Test
    fun testReport() {
        val collector = AtomicMetricCollector("test.report")
        val counter = collector.counter(MetricId("c1"), initialValue = 5)
        val gauge = collector.gauge(MetricId("g1"), initialValue = 12.3)

        val report = collector.report()
        assertEquals(2, report.size)
        assertEquals(5L, report[MetricId("c1")]?.value)
        assertEquals(12.3, report[MetricId("g1")]?.value)
    }
}