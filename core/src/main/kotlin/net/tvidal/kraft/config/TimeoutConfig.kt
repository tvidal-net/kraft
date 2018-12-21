package net.tvidal.kraft.config

data class TimeoutConfig(
    val heartbeat: Int,
    val election: IntRange,
    val firstElection: Int
) {

    val minElection: Int
        get() = election.first

    val maxElection: Int
        get() = election.last
}
