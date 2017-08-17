package net.tvidal.kraft.config

data class TimeoutConfig(

  val heartbeat: Int,

  val minElectionTimeout: Int,

  val maxElectionTimeout: Int,

  val firstElectionTimeout: Int

)
