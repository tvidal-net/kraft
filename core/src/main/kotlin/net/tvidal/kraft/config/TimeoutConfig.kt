package net.tvidal.kraft.config

interface TimeoutConfig {

    val heartbeat: Int

    val minElectionTimeout: Int

    val maxElectionTimeout: Int

    val firstElectionTimeout: Int

}
