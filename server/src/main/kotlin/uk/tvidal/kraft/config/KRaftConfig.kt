package uk.tvidal.kraft.config

import uk.tvidal.kraft.domain.RaftCluster
import uk.tvidal.kraft.storage.KRaftStorage
import uk.tvidal.kraft.transport.KRaftTransport

data class KRaftConfig(
    val cluster: RaftCluster,
    val transport: KRaftTransport,
    val storage: KRaftStorage,
    val timeout: TimeoutConfig = TimeoutConfig(),
    val size: SizeConfig = SizeConfig()
)
