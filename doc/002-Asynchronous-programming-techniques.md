---
typora-root-url: ./assets
---

# 1 summary

- Callback
- suspend & resume
- Cancel, Retry, Timeout
- Error handler


![image-20210304141129264](/image-20210304141129264.png)

# 2 run coroutines

| function           | 描述                                                        | 执行    |
| ------------------ | ----------------------------------------------------------- | ------- |
| GlobalScope.launch | 没有返回值                                                  | join()  |
| async              | 有返回值                                                    | await() |
| runBlocking        | 主要被用来在main函数中或者测试中使用,连接阻塞和非阻塞的世界 |         |

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





