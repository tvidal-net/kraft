package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.engine.RaftServer
import uk.tvidal.kraft.loadResource
import uk.tvidal.kraft.logging.KRaftLogging
import java.lang.Thread.yield
import java.util.concurrent.ThreadLocalRandom

abstract class ClusterServer internal constructor(
    clusterConfig: List<KRaftServerConfig>
) : KRaftServer {

    internal companion object : KRaftLogging()

    private val random: ThreadLocalRandom
        get() = ThreadLocalRandom.current()

    val nodes: List<RaftEngine> = clusterConfig.map {
        RaftServer(it)
    }

    val size: Int
        get() = nodes.size

    val leader: RaftEngine?
        get() = nodes.firstOrNull { it.role == LEADER }

    val followers: List<RaftEngine>
        get() = nodes.filter { it.role == FOLLOWER }

    val randomNode: RaftEngine
        get() = nodes[random.nextInt(nodes.size)]

    protected fun waitForLeader() {
        while (leader == null) {
            yield()
        }
    }

    protected fun logo() {
        // http://patorjk.com/software/taag/#p=display&f=Ivrit&t=KRaft%200.1%0AServer
        loadResource("/logo/server.txt")
            .forEach(log::info)
    }

    override fun publish(payload: ByteArray) {
        (leader ?: randomNode).publish(payload)
    }
}
