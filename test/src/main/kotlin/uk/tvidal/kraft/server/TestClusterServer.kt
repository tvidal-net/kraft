package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.config.SizeConfig
import uk.tvidal.kraft.config.TimeoutConfig
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.raftCluster
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.transport.LocalTransportFactory

class TestClusterServer(
    nodes: Int,
    val localTransport: LocalTransportFactory = LocalTransportFactory()
) : ClusterServer(
    clusterConfig = nodes.run {
        raftCluster(nodes).map {
            KRaftServerConfig(
                cluster = it,
                transport = localTransport.create(it.self),
                storage = RingBufferStorage(),
                timeout = timeout,
                sizes = sizes
            )
        }
    }
) {

    internal companion object : KRaftLogging() {
        private val timeout = TimeoutConfig()
        private val sizes = SizeConfig()
    }

    override fun start() {
        nodes.forEach { it.work(0L) }
    }

    override fun stop() {}

    override fun join() {}

    override fun waitForLeader() {
        if (leader == null) {
            val leader = nodes[0]
            val now = leader.nextElectionTime + 1L

            repeat(5) {
                nodes.forEach { node -> node.work(now) }
            }
        }
        if (leader?.role != LEADER) {
            throw IllegalStateException("The cluster could not elect a leader")
        }
    }
}
