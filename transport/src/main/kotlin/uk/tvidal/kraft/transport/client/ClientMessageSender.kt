package uk.tvidal.kraft.transport.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.transport.MessageSender

class ClientMessageSender(
    override val self: RaftNode,
    val client: ClientTransport
) : MessageSender {

    internal companion object : KRaftLogging()

    override val node: RaftNode
        get() = client.node

    override fun send(message: Message) {
        log.debug { "[$self] -> $node send $message" }
        client.write(message)
    }

    override fun respond(message: Message) {
        log.debug { "[$self] -> $node respond $message" }
        client.write(message)
    }

    override fun close() {
        client.close()
    }

    override fun toString() = "$javaClassName[$self -> $node]"
}
