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
    val smalls = (1..10).toList().let { s -> s + s }
    val larges = if(special) { listOf(12, 37, 62, 87) } else { listOf(25, 50, 75, 100) }

    // How many big numbers do you want?
    val numberOfBig = Random.nextInt(1..4)
    logger.info("Can I have $numberOfBig big ones, please?")

    // Then how many small?
    val numberOfSmall = 6 - numberOfBig
    logger.info("And $numberOfSmall small ones, thanks.")

    // TODO - Support specific numbers - aka play along mode

    // Cards are picked and laid out, smalls first then the large, but right to left.
    val picked: List<Int> = larges.pickRandom(numberOfBig) + smalls.pickRandom(numberOfSmall)

    logger.info("And your numbers are...")
    logger.info(picked.joinToString("  "))

    // ### - a three digit number
    val target = Random.nextInt(100..999)

    logger.info("With a target of $target")

    // Start the clock
    val clock = launch {
        clockLogger.info("START")
        var ticks = 0
        while (ticks++ < 30) {
            delay(1.seconds)
            continuedLogger.info(".")
        }
        continuedLogger.info("END\n")
    }

    // Attempt to solve the problem
    val solutionAttempt = Channel<Solution?>(1)
    val solve = launch {
        calculateSolution(picked, target, solutionAttempt)
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
        logger.info("I got ${solution.result}")
    }
}

fun List<Int>.pickRandom(times: Int): List<Int> = 1.rangeTo(times).map { random() }