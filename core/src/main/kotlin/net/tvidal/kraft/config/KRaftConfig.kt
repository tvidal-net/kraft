package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftCluster
import net.tvidal.kraft.storage.KRaftStorage
import net.tvidal.kraft.transport.KRaftTransport

data class KRaftConfig(
    val cluster: RaftCluster,
    val transport: KRaftTransport,
    val storage: KRaftStorage,
    val timeout: TimeoutConfig = TimeoutConfig(),
    val size: SizeConfig = SizeConfig()
)
