package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.engine.RaftEngineImpl
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import java.util.concurrent.ThreadLocalRandom

internal abstract class ClusterServer(
    clusterConfig: List<KRaftConfig>
) : KRaftServer {
    companion object : KRaftLogging()

    private val random: ThreadLocalRandom
        get() = ThreadLocalRandom.current()

    protected val nodes: List<RaftEngine> = clusterConfig.map {
        RaftEngineImpl(it)
    }

    protected val leader: RaftEngine?
        get() = nodes.firstOrNull { it.role == LEADER }

    protected val followers: List<RaftEngine>
        get() = nodes.filter { it.role == FOLLOWER }

    protected val randomNode: RaftEngine
        get() = nodes[random.nextInt(nodes.size)]

    override fun publish(data: List<ByteArray>) {
        randomNode.run {
            publish(entries(data.map { entryOf(it) }))
        }
    }
}