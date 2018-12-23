package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message
import java.util.concurrent.ConcurrentHashMap

class LocalTransportFactory {

    private val receivers = ConcurrentHashMap<RaftNode, MessageReceiver>()

    private val senders = ConcurrentHashMap<RaftNode, MessageSender>()

    fun create(node: RaftNode) = object : KRaftTransport {

        override fun sender(node: RaftNode) = senders.computeIfAbsent(node) {
            LocalSender(node)
        }

        override fun receiver(): MessageReceiver = receivers.computeIfAbsent(node) {
            DualQueueMessageReceiver()
        }
    }

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
