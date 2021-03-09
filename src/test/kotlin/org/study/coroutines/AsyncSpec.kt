package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    "composing suspend functions"{
        a()
    }
})

val log = org.slf4j.LoggerFactory.getLogger(AsyncSpec::class.java)
suspend fun one(): Int {
    delay(1000)
    return 1
}

suspend fun two(): Int {
    delay(2000)
    return 2
}

suspend fun a(){
    delay(100)
    log.info("--a--")
    b()
    coroutineScope {
        launch {
            c()
            d()
        }
        launch{
            e()
        }
    }
}
suspend fun b(){
    delay(100)
    log.info("--b--")
}
suspend fun c(){
    delay(100)
    log.info("--c--")
}
suspend fun d(){
    delay(100)
    log.info("--d--")
}
suspend fun e(){
    delay(100)
    log.info("--e--")
}