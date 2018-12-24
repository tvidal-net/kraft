package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message

open class ServerMessageSender(
    override val node: RaftNode,
    val server: ServerTransport
) : MessageSender {

    final override val self: RaftNode
        get() = server.self

    override fun send(message: Message) {
        server.write(node, message)
    }

    override fun respond(message: Message) {
        server.write(node, message)
    }
}
