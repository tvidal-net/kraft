package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message
import java.io.Closeable

class ClusterMessageSender(
    node: RaftNode,
    server: ServerTransport,
    val client: ClientTransport
) : NetworkMessageSender(node, server), Closeable {

    override fun send(message: Message) {
        client.write(message)
    }

    override fun close() {
        client.close()
    }
}
