---
typora-root-url: ./assets
---

# 1 Main Thread

```
package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import kotlin.concurrent.thread

class HelloWorldSpec : StringSpec({
    /**
    main program starts: pool-2-thread-1 @coroutine#4
    main program finished: pool-2-thread-1 @coroutine#4
    Fake work starts: Thread-4
     */
    "main spec"{
        startAndFinish("main program") {
            thread {
                startAndFinish("Fake work") {
                    Thread.sleep(1000) //pretend 
                }
            }
        }
    }
})

/**
main program starts: main
main program finished: main
Fake work starts: Thread-0
Fake work finished: Thread-0
 */
fun main() {
    startAndFinish("main program") {
        thread {
            startAndFinish("Fake work") {
                Thread.sleep(1000)
            }
        }
    }
}

fun startAndFinish(prefixMessage: String, action: () -> Unit) {
    println("$prefixMessage starts: ${Thread.currentThread().name}")
    action()
    println("$prefixMessage finished: ${Thread.currentThread().name}")
}
```

#  2 并发与并行

## 2.1 名次解释

1. 是近义词吗？

答： 不是。并发 concurrent，并行 parallel

2. 有什么区别？

答： 并发，一个人，一边唱歌，一边跳舞。同时做。

并行，两个人，一个唱歌，一个跳舞。也是同时做，不过是两个人各做各的，互不影响

## 2.2 操作系统

![6544919-a81b011b72b3b7f2](assets/6544919-a81b011b72b3b7f2.png)



操作系统对“并发”和“并行”有特殊的应用场景，其中，

- 并发。虽然CPU在同一时间只能做一件事，但是，CPU的运算速度很快。比如，一边听歌，一边打游戏，那么，只要时间间隔控制得当，CPU可以很快切换听歌和打游戏的任务，中间有”停顿”，但用户察觉不出来。看起来，就是并发了。
- 并行。多核CPU

**并发会抢占资源！！！**

## 2.3 软件模型

1. Callback
2. Promise
3. Actor
4. Publish/Subscribe

# 3 what's coroutines

- Process
- Thread
- Coroutines，在kotlin中，特指`官方提供的一套线程API`。可以用`看起来`同步的代码，实现异步与回调的效果（`非阻塞式挂起`)
  - Concurrent
  - Parallel

## 3.1 为什么coroutines不会阻塞Thread？

- 使用`suspend`关键字，标记需要异步执行的function
- 每个function，维护一个独立的上下文
- 使用`actor`模型，管理一系列互相调用的`suspend function`
- 使用`publish/suscribe`模型，实现消息的自动通知，从而，在方法执行完毕后，自动切回来

> API风格是`Promise`

## 3.2 coroutine是轻量级线程吗？

> 答：不是

用`Java`的`Executors`也可以使用相同的功能，只是代码稍微多点

```
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LotsOfCoroutinesSpec : StringSpec({
    "test the kotlin official example"{// 5s 548ms
        repeat(100_000) {
            launch {
                delay(5000L)
                print(".")
            }
        }
    }
    "test the example of Java version"{// 5s 291ms
        val executor = Executors.newSingleThreadScheduledExecutor()
        val task = Runnable {
            print(".")
        }
        repeat(100_000) {
            executor.schedule(task, 5L, TimeUnit.SECONDS)
        }
        executor.shutdown()
        executor.awaitTermination(10L, TimeUnit.SECONDS)
    }
})
```

# 4 Refactoring

## 4.1 dependency

```
dependencies {
    val kotlinCoroutinesVersion = "1.4.2"
    val kotestVersion = "4.4.0.RC2"
    val slf4jVersion = "1.7.30"
    val log4jVersion = "2.14.0"

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}
```

## 4.2 log4j2.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn" name="MyApp" packages="">
    <Appenders>
        <Console name="STDOUT" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="STDOUT"/>
        </Root>
    </Loggers>
</Configuration>
```

## 4.3 Testing

```
package org.study.coroutines

import io.kotest.core.spec.style.StringSpec

class AwaitSpec:StringSpec({
    "logs"{
        log.info("hello world")
    }
}){
    companion object{
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}
```

## 4.4 source code

```
package org.study.coroutines

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger

class HelloWorldSpec : StringSpec({
    "main spec"{
        runBlocking {
            startAndFinish(log,"main program") {
                startAndFinish(log,"Fake work") {
                    delay(1000)
                }
            }
        }
    }
}){
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun startAndFinish(log: Logger, prefixMessage: String, action: suspend () -> Unit) {
    log.info("$prefixMessage starts: ${Thread.currentThread().name}")
    action()
    log.info("$prefixMessage finished: ${Thread.currentThread().name}")
}
```



