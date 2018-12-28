package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import java.io.Closeable

class ClusterMessageSender(
    node: RaftNode,
    server: ServerTransport,
    val client: ClientTransport
) : ServerMessageSender(node, server), Closeable {

    internal companion object : KRaftLogging()

    override fun send(message: Message) {
        log.debug { "[$self] => $node send $message" }
        client.write(message)
    }

    override fun close() {
        client.close()
    }

    override fun toString() = "${javaClass.simpleName}[$self => $node]"
}
