package org.study.coroutines.sequence

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe

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
    "sequence"{
        val resultList = listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89)
        fibonacciSequence()
            .takeWhile { it < 100 }
            .forEachIndexed { index, i ->
                log.info("index: $index, value: $i")

                resultList[index].shouldBe(i)
            }
    }
}) {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(this::class.java)
    }
}

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