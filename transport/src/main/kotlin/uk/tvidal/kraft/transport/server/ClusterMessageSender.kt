package uk.tvidal.kraft.transport.server

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.javaClassName
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.transport.client.ClientTransport

class ClusterMessageSender(
    node: RaftNode,
    server: ServerTransport,
    val client: ClientTransport
) : ServerMessageSender(node, server) {

    internal companion object : KRaftLogging()

    override fun send(message: Message) {
        log.debug { "[$self] -> $node send $message" }
        client.write(message)
    }

    override fun close() {
        client.close()
    }

    override fun toString() = "$javaClassName[$self -> $node]"
}
