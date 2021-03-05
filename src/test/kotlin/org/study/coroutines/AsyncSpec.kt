package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

class AsyncSpec : StringSpec({
    "suspend & resume"{
        val time = measureTimeMillis {
            log.info("------start-------")
            val one = async { one() }
            val two = async { two() }
            val result = one.await() + two.await()
//            val result = async { one() }.await() + async { two() }.await()
            /*val one = async { one() }.await()
            val two = async { two() }.await()
            val result = one + two*/
            result.shouldBe(3)
            log.info("the result: $result")
        }
        log.info("Time to execute method: $time")
        time.shouldBeBetween(2000, 3000)
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun one(): Int {
    delay(1000)
    return 1
}

suspend fun two(): Int {
    delay(2000)
    return 2
}