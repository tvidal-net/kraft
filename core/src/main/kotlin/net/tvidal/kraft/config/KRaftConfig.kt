package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftCluster

data class KRaftConfig(

  val cluster: RaftCluster,

  val timeout: TimeoutConfig,

  val transportFactory: KRaftTransportFactory,

  val logFactory: KRaftLogFactory,

  val sizes: SizeConfig

)
