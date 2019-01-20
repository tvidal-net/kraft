package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.message.Message
import java.util.concurrent.ConcurrentHashMap

class LocalTransportFactory {

    private val receiver = ConcurrentHashMap<RaftNode, MessageReceiver>()

    fun create(node: RaftNode): KRaftTransport = LocalTransport(node)

    private inner class LocalMessageSender(
        override val self: RaftNode,
        override val node: RaftNode
    ) : MessageSender {

        override fun send(message: Message) {
            receiver[node]?.offer(message)
        }

        override fun respond(message: Message) {
            send(message)
        }

        override fun close() {}

        override fun toString() = "[$self] -> $node"
    }

    private inner class LocalTransport(
        override val self: RaftNode
    ) : KRaftTransport {

        private val messageReceiver = receiver.computeIfAbsent(self) {
            SimpleMessageReceiver()
        }

        override fun sender(node: RaftNode) = LocalMessageSender(self, node)

        override fun receiver() = messageReceiver

        override fun close() {}

        override fun toString() = "$javaClassName[$self]"
    }
}
