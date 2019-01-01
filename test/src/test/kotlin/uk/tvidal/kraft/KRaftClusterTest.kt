package uk.tvidal.kraft

import org.junit.jupiter.api.BeforeEach
import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.engine.RaftEngine
import uk.tvidal.kraft.server.TestClusterServer
import uk.tvidal.kraft.transport.MessageSender

abstract class KRaftClusterTest(nodes: Int = 2) {

    val cluster = TestClusterServer(nodes)

    val servers = cluster.nodes
        .associateBy(RaftEngine::self)

    val client = clientNode("Client")

    val clientTransport = cluster.localTransport.create(client)

    val clientMessages = clientTransport.receiver()

    val leader get() = cluster.leader!!.self

    val follower get() = cluster.followers.first().self

    @BeforeEach
    internal fun setUp() {
        cluster.waitForLeader()
    }

    fun sender(to: RaftNode): MessageSender = clientTransport.sender(to)

    fun work(now: Long = 0L) = servers.values.forEach { it.work(now) }

    fun work(node: RaftNode, now: Long = 0L) = servers[node]!!.work(now)

    fun triggerElection(node: RaftNode = follower) {
        val server = servers[node]!!
        val now = server.nextElectionTime + 1L

        // Trigger Election
        server.work(now)

        // Become Follower
        work(leader, now)
    }
}
