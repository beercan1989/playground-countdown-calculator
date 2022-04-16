package uk.co.baconi.playground.countdown

import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.random.nextInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.time.Duration.Companion.seconds

fun main() = runBlocking {

    val logger = LoggerFactory.getLogger("Countdown")
    val clockLogger = LoggerFactory.getLogger("Clock")
    val continuedLogger = LoggerFactory.getLogger("Continued")

    val special = false
    val smalls: MutableList<Int> = (1..10).let { s -> (s + s) }.toMutableList()
    val larges = if(special) { mutableListOf(12, 37, 62, 87) } else { mutableListOf(25, 50, 75, 100) }

    // How many big numbers do you want?
    val numberOfBig = Random.nextInt(1..4)
    logger.info("Can I have $numberOfBig big ones, please?")

    // Then how many small?
    val numberOfSmall = 6 - numberOfBig
    logger.info("And $numberOfSmall small ones, thanks.")

    // TODO - Support specific numbers - aka play along mode

    // Cards are picked and laid out, smalls first then the large, but right to left.
    val picked = mutableListOf<Int>()
    for (i in 1..numberOfBig) {
        picked.add(larges.removeAt(Random.nextInt(larges.size)))
    }
    for (i in 1..numberOfSmall) {
        picked.add(smalls.removeAt(Random.nextInt(smalls.size)))
    }

    logger.info("And your numbers are...")
    logger.info(picked.joinToString("  "))

    // ### - a three digit number
    val target = Random.nextInt(100..999)

    logger.info("With a target of $target")

    val isPlayerAnAss = true

    // Start the clock
    val clock = launch {
        if(isPlayerAnAss) logger.info("Clock START")
        else clockLogger.info("START")
        var ticks = 0
        while (ticks++ < 30) {
            delay(1.seconds)
            if(!isPlayerAnAss) continuedLogger.info(".")
        }
        if(isPlayerAnAss) logger.info("Clock END")
        else continuedLogger.info("END\n")
    }

    // Attempt to solve the problem
    val solutionAttempt = Channel<Solution?>(1)
    val solve = launch {
        calculateSolution(picked, target, solutionAttempt, isPlayerAnAss)
    }

    // Wait for the countdown to finish
    clock.join()
    logger.info("So what did you get?")

    // Stop trying to find a solution
    solve.cancelAndJoin()

    val solution = solutionAttempt.receive()
    if(solution == null) {
        logger.info("I didn't get anything...")
    } else {
        if(solution.distance == 0) {
            logger.info("I got ${solution.method.result} by doing ${solution.method}")
        } else {
            logger.info("I got ${solution.distance} away, with ${solution.method.result}; by doing ${solution.method}")
        }
    }
}
