package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.Logger

class HelloWorldSpec : StringSpec({
    "main spec"{
        startAndFinish(log, "main program") {
            startAndFinish(log, "Fake work") {
                delay(5000)
            }
        }
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun startAndFinish(log: Logger, prefixMessage: String, action: suspend () -> Unit) {
    log.info("$prefixMessage starts: ${Thread.currentThread().name}")
    action()
    log.info("$prefixMessage finished: ${Thread.currentThread().name}")
}