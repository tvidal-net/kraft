package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message

open class ServerMessageSender(
    override val node: RaftNode,
    val server: ServerTransport
) : MessageSender {

    internal companion object : KRaftLogging()

    final override val self: RaftNode
        get() = server.self

    override fun send(message: Message) {
        log.debug { "[$self] => $node send $message" }
        server.write(node, message)
    }

    override fun respond(message: Message) {
        log.debug { "[$self] => $node respond $message" }
        server.write(node, message)
    }

    override fun toString() = "${javaClass.simpleName}[$self => $node]"
}
