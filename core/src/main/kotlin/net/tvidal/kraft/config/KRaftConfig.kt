package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftCluster

interface KRaftConfig {

    val cluster: RaftCluster

    val timeout: TimeoutConfig

    val transport: TransportConfig<TransportNodeConfig>

    val log: LogConfig

    val size: SizeConfig

}
