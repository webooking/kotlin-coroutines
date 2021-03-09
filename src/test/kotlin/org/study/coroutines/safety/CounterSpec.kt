package org.study.coroutines.safety

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

class CounterSpec : StringSpec({
    "single Thread count"{
        val count = singleThreadCount(log)
        log.info("result: $count")
        count.shouldBe(1000_000)
    }
    "atomic integer"{
        val count = atomicIntegerCount(log)
        log.info("result: $count")
        count.shouldBe(1000_000)
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun singleThreadCount(log:Logger): Int {
    var counter = 0
    val time = measureTimeMillis {
        withContext(Executors.newSingleThreadScheduledExecutor().asCoroutineDispatcher()) {
            repeat(1000) { index ->
                launch {
                    val temp = counter
                    log.info("index: $index -- start -- counter: $counter")
                    repeat(1000) {
                        counter++
                    }
                    log.info("index: $index -- end -- counter: $counter, step: ${counter - temp}")
                }
            }
        }
    }
    log.info("Execution Time = $time ms")
    return counter
}

suspend fun atomicIntegerCount(log:Logger): Int {
    val counter = AtomicInteger()
    val time = measureTimeMillis {
        withContext(Dispatchers.Unconfined) {
            repeat(1000) { index ->
                launch {
                    val temp = counter.get()
                    log.info("index: $index -- start -- counter: ${counter.get()}")
                    repeat(1000) {
                        counter.incrementAndGet()
                    }
                    log.info("index: $index -- end -- counter: ${counter.get()}, step: ${counter.get() - temp}")
                }
            }
        }
    }
    log.info("Execution Time = $time ms")
    return counter.get()
}