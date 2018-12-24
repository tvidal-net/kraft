package uk.tvidal.kraft.config

import uk.tvidal.kraft.RaftCluster
import uk.tvidal.kraft.storage.KRaftStorage
import uk.tvidal.kraft.transport.KRaftTransport

data class KRaftServerConfig(
    val cluster: RaftCluster,
    val transport: KRaftTransport,
    val storage: KRaftStorage,
    val timeout: TimeoutConfig = TimeoutConfig(),
    val sizes: SizeConfig = SizeConfig()
)
