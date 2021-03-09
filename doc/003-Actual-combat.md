---
typora-root-url: ./assets
---

# 1 Fibonacci sequence

斐波那契数列：

| *n =* | *0*  | *1*  | *2*  | *3*  | *4*  | *5*  | *6*  | *7*  | *8*  | *9*  | *10* | *11* | *12* | *13* | *14* | *15* | *……* |
| ----- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- | ---- |
| xn =  | 0    | 1    | 1    | 2    | 3    | 5    | 8    | 13   | 21   | 34   | 55   | 89   | 144  | 233  | 377  | 610  | ……   |

## 1.1 求xn

<img src="/image-20210306152119226.png" alt="image-20210306152119226" style="zoom:30%;float:left;" />

```
package org.study.coroutines.sequence

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import java.lang.AssertionError

class FibonacciSpec : StringSpec({
    "n<0"{
        fibonacci(-1)
    }
    "xn"{
        table(
            headers("n", "xn"),
            row(0, 0),
            row(1, 1),
            row(2, 1),
            row(3, 2),
            row(4, 3),
            row(13, 233),
        ).forAll { n, x ->
            fibonacci(n).shouldBe(x)
        }
    }
})

val log = org.slf4j.LoggerFactory.getLogger(FibonacciSpec::class.java)

fun fibonacci(n: Int): Int {
    if (n < 0) {
        throw AssertionError("The parameter cannot less than 0")
    }
    return when (n) {
        0 -> 0
        1 -> 1
        else -> fibonacci(n - 1) + fibonacci(n - 2)
    }
}
```

## 1.2 构建一个无穷的Fibonacci sequence

```
fun fibonacciSequence() = sequence<Int> {
    yield(0)
    var current = 0
    var next = 1
    while (true) {
        yield(next)
        val temp = current + next
        current = next
        next = temp
    }
}
```



```
"sequence"{
    val resultList = listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89)
    fibonacciSequence()
        .takeWhile { it < 100 }
        .forEachIndexed { index, i ->
            log.info("index: $index, value: $i")
            resultList[index].shouldBe(i)
        }
}
```

# 2 Thread Safety

参考文档：https://blog.dreamtobe.cn/kotlin-coroutines/

多个线程共享同一个数据时，是存在线程安全的。

> 如果这些线程只是`查询`共享数据，那么，无所谓线程安全了。

## 2.1 story

- 定义一个全局变量 counter
- 循环1000次，每次创建一个`launch`
- 每个`launch`中，循环1000次，做`counter++`

请问，任务执行结束后，counter是多少？

| 方案                   | 执行用时 |
| ---------------------- | -------- |
| Mutex                  | 48894 ms |
| AtomicInteger          | 1568 ms  |
| newSingleThreadContext | 3113 ms  |
| Actor                  | 14192 ms |

## 2.2 Single Thread

```
package org.study.coroutines.safety

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.*
import org.slf4j.Logger
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class CounterSpec : StringSpec({
    "test count"{
        log.info("result: ${count(log)}")
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun count(log:Logger): Int {
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
    log.info("Execution Time = $time ms") // 140ms
    return counter
}
```

## 2.3 AtomicInteger

```
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
    log.info("Execution Time = $time ms") // 86ms
    return counter.get()
}
```

# 3 银行排队叫号

## 3.1 story

![image-20210308122713505](/image-20210308122713505.png)

- 1秒内，（随机）间隔1～3毫秒，生成2～5个顾客
- 顾客
  - 自动加入队列
  - 轮到这个客户时，自动前往空闲的服务窗口
  - 等待时间超过5毫秒，自动退出队列
  - 不管是服务结束，还是超时退出，都自动退出队列
- 服务窗口
  - 有3个服务窗口
  - 每个顾客需要服务2毫秒
- 1秒后，等待队列清空，任务停止

请问，如何设计排队系统，超时退出的客户最少？

