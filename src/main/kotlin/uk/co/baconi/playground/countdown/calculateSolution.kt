package uk.co.baconi.playground.countdown

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import kotlin.math.abs

enum class Operation(val perform: (Int, Int) -> Int, private val description: String) {
    Add(Int::plus, "+"),
    Minus(Int::minus,"-"),
    Times(Int::times,"*"),
    Divide(Int::div, "/");
    override fun toString() = description
}

data class Method(val result: Int, private val description: String) {
    constructor(number: Int) : this(number, "$number")
    constructor(left: Method, operation: Operation, right: Int) : this(
        operation.perform(left.result, right),
        "($left $operation $right)"
    )
    override fun toString() = description
}

data class Solution(val distance: Int, val method: Method)

suspend fun CoroutineScope.calculateSolution(picked: List<Int>, target: Int, solution: Channel<Solution?>) {

    var bestSolution: Solution? = null

    // Numbers: 6
    // Operations: 4

    // TODO - Validate this assumption based on only using the numbers once.
    // Possible number combinations: 1 digit to 6 digits from the 6 numbers provided
    // 6^1 + 6^2 + 6^3 + 6^4 + 6^5 + 6^6 = 55,986

    // Possible solutions to check = set of possible numbers * mathematical operations
    // 6^1 + (6^2 + 6^3 + 6^4 + 6^5 + 6^6) * 4 = 223,926

    val operations = Operation.values().asSequence()
    val numbers = picked.asSequence()

    // TODO - Protect against negative results
    // TODO - Protect against fractional results
    // TODO - Protect against reusing the same card number
    val numberCombinations: Sequence<Method> = sequence {

        // 6^2
        for (first in numbers.map(::Method)) {
            yield(first)

            // 6^2
            for(operation1 in operations) {
                for (second in numbers.map { Method(first, operation1, it) }) {
                    yield(second)

                    // 6^3
                    for(operation2 in operations) {
                        for (third in numbers.map { Method(second, operation2, it) }) {
                            yield(third)

                            // 6^4
                            for (operation3 in operations) {
                                for (fourth in numbers.map { Method(third, operation3, it) }) {
                                    yield(fourth)

                                    // 6^5
                                    for (operation4 in operations) {
                                        for (fifth in numbers.map { Method(fourth, operation4, it) }) {
                                            yield(fifth)

                                            // 6^6
                                            for (operation5 in operations) {
                                                for (sixth in numbers.map { Method(fifth, operation5, it) }) {
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
                val distance = abs(target - attempt.result)
                if (distance <= 10) {
                    bestSolution = Solution(distance, attempt)
                }
            }
            else -> {
                val distance = abs(target - attempt.result)
                if(distance < bestSolution.distance) {
                    bestSolution = Solution(distance, attempt)
                }
            }
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