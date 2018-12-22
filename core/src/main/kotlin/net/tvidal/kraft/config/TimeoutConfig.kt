package net.tvidal.kraft.config

import java.util.Random

data class TimeoutConfig(
    val heartbeatTimeout: Int = 500,
    val minElectionTimeout: Int = heartbeatTimeout * 3,
    val maxElectionTimeout: Int = minElectionTimeout * 2,
    val firstElectionTimeout: Int = 0
) {
    private val random = Random()

    val randomElectionTimeout: Int
        get() = random.nextInt(maxElectionTimeout - minElectionTimeout + 1) + minElectionTimeout
}
