package uk.co.baconi.playground.countdown

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import kotlin.math.abs

enum class Operation(val perform: (Int, Int) -> Int, private val description: String) {
    Add(Int::plus, "+"),
    Minus(Int::minus, "-"),
    Times(Int::times, "*"),
    Divide(Int::div, "/");

    override fun toString() = description
}

data class Method(val result: Lazy<Int>, val numbers: List<Int>, private val description: String) {
    constructor(number: Int) : this(lazyOf(number), listOf(number), "$number")
    constructor(left: Method, operation: Operation, right: Int) : this(
        lazy { operation.perform(left.result.value, right) },
        left.numbers + right,
        "($left $operation $right)"
    )

    override fun toString() = description
}

data class Solution(val distance: Int, val method: Method)

fun Method.hasNoOverUseOf(available: List<Int>): Boolean {
    return available.groupBy { n ->
        n
    }.map { (key, value) ->
        numbers.count { number -> key == number } <= value.size
    }.reduce { left, right ->
        if (!right) { false } else { left }
    }
}

suspend fun CoroutineScope.calculateSolution(picked: List<Int>, target: Int, solution: Channel<Solution?>, isPlayerAnAss: Boolean) {

    var bestSolution: Solution? = null

    val operations = Operation.values().asSequence()
    val numbers = picked.asSequence()

    // TODO - Protect against negative results
    // TODO - Protect against fractional results
    val numberCombinations: Sequence<Method> = sequence {

        // 6^2
        for (first in numbers.map(::Method)) {
            yield(first)

            // 6^2
            for (operation1 in operations) {
                for (second in numbers.map { Method(first, operation1, it) }.filter { m -> m.hasNoOverUseOf(picked) }) {
                    yield(second)

                    // 6^3
                    for (operation2 in operations) {
                        for (third in numbers.map { Method(second, operation2, it) }.filter { m -> m.hasNoOverUseOf(picked) }) {
                            yield(third)

                            // 6^4
                            for (operation3 in operations) {
                                for (fourth in numbers.map { Method(third, operation3, it) }.filter { m -> m.hasNoOverUseOf(picked) }) {
                                    yield(fourth)

                                    // 6^5
                                    for (operation4 in operations) {
                                        for (fifth in numbers.map { Method(fourth, operation4, it) }.filter { m -> m.hasNoOverUseOf(picked) }) {
                                            yield(fifth)

                                            // 6^6
                                            for (operation5 in operations) {
                                                for (sixth in numbers.map { Method(fifth, operation5, it) }.filter { m -> m.hasNoOverUseOf(picked) }) {
                                                    yield(sixth)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Perform solution calculations
    for (attempt: Method in numberCombinations) {
        when {
            !isActive -> break
            bestSolution?.distance == 0 -> break
            bestSolution == null -> {
                val distance = abs(target - attempt.result.value)
                if (distance <= 10) {
                    bestSolution = Solution(distance, attempt)
                }
            }
            else -> {
                val distance = abs(target - attempt.result.value)
                if (distance < bestSolution.distance) {
                    bestSolution = Solution(distance, attempt)
                }
            }
        }
    }

    if(isPlayerAnAss) {
        val logger = LoggerFactory.getLogger("Countdown")
        if (isActive && bestSolution == null) {
            logger.info("I GIVE UP!!!!!")
        } else {
            logger.info("Eureka!")
        }
    }

    solution.send(bestSolution)
}

fun checkNotNegative(value: Int): Int {
    if (value < 0) throw IllegalStateException("Required value to be not negative.")
    return value
}

fun checkNoRemainder(left: Int, right: Int): Int {
    if (left % right != 0) throw IllegalStateException("Required remainder to be zero.")
    return left / right
}