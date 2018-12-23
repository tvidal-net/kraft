package uk.tvidal.kraft.server

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.engine.RaftServer
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import java.util.concurrent.ThreadLocalRandom

abstract class ClusterServer internal constructor(
    clusterConfig: List<KRaftConfig>
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

    protected val randomNode: RaftEngine
        get() = nodes[random.nextInt(nodes.size)]

    override fun publish(data: List<ByteArray>) {
        randomNode.run {
            publish(entries(data.map { entryOf(it) }))
        }
    }
}
