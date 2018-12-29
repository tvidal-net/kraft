package uk.tvidal.kraft.config

import uk.tvidal.kraft.HEARTBEAT_TIMEOUT
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.NOW
import java.util.Random

data class TimeoutConfig(
    val heartbeatTimeout: Int = HEARTBEAT_TIMEOUT,
    val minElectionTimeout: Int = heartbeatTimeout * 3,
    val maxElectionTimeout: Int = minElectionTimeout * 2,
    val firstElectionTimeout: Int = 0
) {
    private val random = Random()

    private val randomElectionTimeout: Int
        get() = random.nextInt(maxElectionTimeout - minElectionTimeout + 1) + minElectionTimeout

    internal fun nextElectionTime(now: Long): Long = now + randomElectionTimeout

    internal fun firstElectionTime(now: Long): Long = when (firstElectionTimeout.toLong()) {
        NEVER -> NEVER
        NOW -> now
        else -> now + firstElectionTimeout + randomElectionTimeout - minElectionTimeout
    }
}
