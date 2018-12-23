package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message

open class NetworkMessageSender(
    override val node: RaftNode,
    val server: ServerTransport
) : MessageSender {

    override fun send(message: Message) {
        throw IllegalStateException("Nodes there are not in the cluster can only be responded to")
    }

    override fun respond(message: Message) {
        server.write(node, message)
    }
}
