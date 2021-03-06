package uk.tvidal.kraft.transport.server

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.transport.MessageSender

open class ServerMessageSender(
    override val node: RaftNode,
    val server: ServerTransport
) : MessageSender {

    internal companion object : KRaftLogging()

    final override val self: RaftNode
        get() = server.self

    override fun send(message: Message) {
        log.debug { "[$self] -> $node send $message" }
        server.write(node, message)
    }

    override fun respond(message: Message) {
        log.debug { "[$self] -> $node respond $message" }
        server.write(node, message)
    }

    override fun close() {}

    override fun toString() = "$javaClassName[$self -> $node]"
}
