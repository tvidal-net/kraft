package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.transport.client.ClientTransport
import uk.tvidal.kraft.transport.config.NetworkTransportConfig
import uk.tvidal.kraft.transport.server.ClusterMessageSender
import uk.tvidal.kraft.transport.server.ServerMessageSender
import uk.tvidal.kraft.transport.server.ServerTransport
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class NetworkTransport(
    config: NetworkTransportConfig
) : KRaftTransport {

    override val self = config.self

    private val server = ServerTransport(config)

    private val messages = config.messageReceiver

    private val clients: ConcurrentMap<RaftNode, MessageSender> = ConcurrentHashMap(
        config.others
            .associateWith {
                ClusterMessageSender(
                    node = it,
                    server = server,
                    client = ClientTransport(it, config)
                )
            }
    )

    override fun sender(node: RaftNode): MessageSender {
        if (node == self) {
            throw IllegalArgumentException("[$self] -> $node attempt to get message sender to itself")
        }
        return clients.computeIfAbsent(node) {
            ServerMessageSender(node, server)
        }
    }

    override fun receiver() = messages

    override fun close() {
        server.close()
        clients.values
            .filterIsInstance<Closeable>()
            .forEach(Closeable::close)
    }
}
