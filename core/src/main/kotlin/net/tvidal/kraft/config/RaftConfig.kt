package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftCluster

interface RaftConfig {

    val cluster: RaftCluster

    val timeout: TimeoutConfig

    val transportConfig: TransportConfig<TransportNodeConfig>

    val storageConfig: StorageConfig

}
