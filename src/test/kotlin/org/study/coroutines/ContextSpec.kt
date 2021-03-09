package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.*
import org.slf4j.Logger

class ContextSpec : StringSpec({
    "default"{
        launch { // context of the parent
            doLog(log, "default")
            val job = launch { // context of the parent
                doLog(log, "nested launch -- 1 -- default")
            }
            launch { // context of the parent
                doLog(log, "nested launch -- 2 -- default")
            }
            job.cancelAndJoin()
        }
    }
    "Dispatchers.Default"{
        withContext(Dispatchers.Default) {
            doLog(log, "Dispatchers.Default")
        }
    }
    "Dispatchers.Unconfined"{
        withContext(Dispatchers.Unconfined) {
            doLog(log, "Dispatchers.Unconfined")
        }
    }

    "timeout"{
        try {
            withTimeout(90) {
                repeat(3) {

                    launch {
                        doLog(log, "timeout -- $it")
                    }
                }
            }
        } finally {
            log.info("timeout -- finally --")
        }
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun doLog(log: Logger, prefix: String) {
    log.info("$prefix --start--")
    delay(100)
    log.info("$prefix --finish--")
}