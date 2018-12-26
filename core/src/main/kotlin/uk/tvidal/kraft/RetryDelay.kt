package uk.tvidal.kraft

import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom

data class RetryDelay(
    val maxAttempts: Int = 10,
    val maxDelay: Long = 10_000,
    val initialDelay: Long = 16,
    val fallbackFactor: Double = 2.0
) {
    companion object {
        val FOREVER = RetryDelay(0)
    }

    var current: Long = initialDelay
        private set

    var attempts = maxAttempts
        private set

    val isRunning: Boolean
        get() = maxAttempts == 0 || attempts > 0

    var lastError: Throwable? = null
        private set

    private val jitter: Long
        get() = ThreadLocalRandom.current()
            .nextLong(initialDelay * 2) - initialDelay

    fun catch(e: Throwable) {
        lastError = e
        sleep(current)
        attempts--

        val newDelay = (current * fallbackFactor).toLong()
        current = jitter + if (newDelay <= maxDelay) newDelay else current
    }

    fun reset() {
        lastError = null
        current = initialDelay
        attempts = maxAttempts
    }

    fun rethrow() {
        lastError?.let { throw it }
    }
}
