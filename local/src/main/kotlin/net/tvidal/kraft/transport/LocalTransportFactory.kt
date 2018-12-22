package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message
import java.util.concurrent.ConcurrentHashMap

class LocalTransportFactory {

    private val receivers = ConcurrentHashMap<RaftNode, MessageReceiver>()

    private val senders = ConcurrentHashMap<RaftNode, MessageSender>()

    private val transport = object : KRaftTransport {

        override fun sender(node: RaftNode) = senders.getOrPut(node) { LocalSender(node) }

        override fun register(node: RaftNode, receiver: MessageReceiver) {
            receivers[node] = receiver
        }
    }

    fun create() = transport

    private inner class LocalSender(override var node: RaftNode) : MessageSender {

        private var messageReceiver: MessageReceiver? = null
            get() {
                if (field == null) {
                    field = receivers[node]
                }
                return field
            }

        override fun respond(message: Message) {
            send(message)
        }

        override fun send(message: Message) {
            messageReceiver?.offer(message)
        }
    }
}
