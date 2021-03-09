---
typora-root-url: ./assets
---

# 1 summary

- Callback
- suspend & resume
- Cancel, Retry, Timeout
- Error handler


![image-20210304141129264](/image-20210304141129264.png)

![image-20210306154215968](/image-20210306154215968.png)



# 2 run coroutines

| function    | 描述                                                        | 执行    |
| ----------- | ----------------------------------------------------------- | ------- |
| launch      | 没有返回值                                                  | join()  |
| async       | 有返回值                                                    | await() |
| runBlocking | 主要被用来在main函数中或者测试中使用,连接阻塞和非阻塞的世界 |         |

```
package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeBetween
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
```

# 3 Context

| Dispatchers | desc                                                         |
| ----------- | ------------------------------------------------------------ |
|             | context of the parent                                        |
| Default     | JVM上的共享线程池。通常用来做CPU密集型工作, 比如排序或复杂计算等. |
| ~~Main~~    | 需要添加依赖, 比如`kotlinx-coroutines-android`               |
| Unconfined  |                                                              |
| IO          | 执行IO读写或网络请求                                         |

```
package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import org.slf4j.Logger

class ContextSpec:StringSpec({
    "default"{
        launch {
            doLog(log, "default")
        }
    }
    "Dispatchers.Default"{
        withContext(Dispatchers.Default) {
            doLog(log, "Dispatchers.Default")
        }
    }
    "Dispatchers.Unconfined"{
        withContext(Dispatchers.Unconfined) {
            async {
                delay(100)
                1
            }.await()
            doLog(log,"Dispatchers.Unconfined")
        }
    }
    "withContext & launch"{
        withContext(Dispatchers.Default) {
            doLog(log, "Dispatchers.Default")
            launch {
                doLog(log, "nested launch")
            }
        }
    }
}){
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun doLog(log: Logger, prefix:String){
    log.info("$prefix --start--")
    delay(100)
    log.info("$prefix --finish--")
}
```

# 4 Scope

```
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
```

# 5 timeout

```
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
```





