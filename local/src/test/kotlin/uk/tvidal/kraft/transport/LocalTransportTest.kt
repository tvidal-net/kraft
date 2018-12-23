package uk.tvidal.kraft.transport

import org.junit.jupiter.api.Test
import uk.tvidal.kraft.RaftCluster
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.raftNodes
import kotlin.test.assertSame

class LocalTransportTest {

    private val factory = LocalTransportFactory()
    private val transport = factory.create()

    private val nodes = raftNodes(2)
    private val cluster = RaftCluster(0, nodes)

    val mockReceiver = object : MessageReceiver {

        var message: Message? = null

        override val size = 0

        override fun poll() = message

        override fun offer(message: Message): Boolean {
            this.message = message
            println(message)
            return true
        }

        override fun removeIf(predicate: (Message) -> Boolean) = TODO()
    }

    @Test
    fun `should always return the same transport instance`() {
        assertSame(transport, factory.create())
    }

    @Test
    fun `should deliver message for registered receiver`() {
        transport.register(mockReceiver)
        val message = VoteMessage(nodes[1], 2L, false)
        transport.sender(nodes[0]).send(message)

        assertSame(message, mockReceiver.poll())
    }
}
