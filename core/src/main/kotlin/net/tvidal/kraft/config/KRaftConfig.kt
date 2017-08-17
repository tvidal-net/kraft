package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftCluster

data class KRaftConfig(

  val cluster: RaftCluster,

  val timeout: TimeoutConfig,

  val transport: TransportFactory,

  val log: LogFactory,

  val size: SizeConfig

)
