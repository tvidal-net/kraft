package net.tvidal.kraft

import net.tvidal.kraft.domain.RaftCluster
import net.tvidal.kraft.message.Message
import net.tvidal.kraft.message.raft.VoteMessage
import net.tvidal.kraft.transport.LocalTransportFactory
import net.tvidal.kraft.transport.MessageReceiver
import org.testng.annotations.Test
import kotlin.test.assertSame

@Test
class LocalTransportTest {

    private val factory = LocalTransportFactory()
    private val transport = factory.create()

    private val nodes = raftNodes(2)
    private val cluster = RaftCluster(0, nodes)

    val mockReceiver = object : MessageReceiver {

        var message: Message? = null

        override val size = 0

        override fun poll() = message!!

        override fun offer(message: Message): Boolean {
            this.message = message
            println(message)
            return true
        }
    }

    fun `should always return the same transport instance`() {
        assertSame(transport, factory.create())
    }

    fun `should deliver message for registered receiver`() {
        transport.register(nodes[0], mockReceiver)
        val message = VoteMessage(nodes[1], 2L, false)
        transport.sender(nodes[0]).send(message)

        assertSame(message, mockReceiver.poll())
    }
}
