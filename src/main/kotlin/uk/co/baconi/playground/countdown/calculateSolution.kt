package uk.co.baconi.playground.countdown

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class Method
data class Solution(val distance: Int, val result: Int, val method: Method)

suspend fun CoroutineScope.calculateSolution(picked: List<Int>, target: Int, solution: Channel<Solution?>) {

    var bestSolution: Solution? = null
    val possibleSolutions = Channel<Solution>()

    coroutineScope {
        launch {
            // TODO - Swan sub tasks to discover a possible solution

            // Numbers: 6
            // Operations: 4
            // Possible number combinations: 1 digit to 6 digits from the 6 numbers provided
            // Possible solutions to check = set of possible numbers * operations
        }
    }

    // Shutdown mechanism for the solution channel
    coroutineScope {
        launch {
            while (true) {
                if(isActive) {
                    delay(50)
                } else {
                    possibleSolutions.close()
                }
            }
        }
    }

    for(possibleSolution in possibleSolutions) {
        bestSolution = when {
            bestSolution == null -> possibleSolution
            possibleSolution.distance < bestSolution.distance -> possibleSolution
            else -> bestSolution
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