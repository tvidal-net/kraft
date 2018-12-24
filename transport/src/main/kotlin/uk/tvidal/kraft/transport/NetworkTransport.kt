package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class NetworkTransport(
    config: NetworkTransportConfig
) : KRaftTransport, Closeable {

    override val self = config.self
    private val cluster = config.cluster
    private val codec = config.codec

    private val server = ServerTransport(config)

    private val messages = config.messageReceiver

    private val clients: ConcurrentMap<RaftNode, MessageSender> = ConcurrentHashMap(
        config
            .others
            .associate {
                it to ClusterMessageSender(
                    node = it,
                    server = server,
                    client = ClientTransport(it, config)
                )
            }
    )

    override fun sender(node: RaftNode): MessageSender = clients.computeIfAbsent(node) {
        ServerMessageSender(node, server)
    }

    override fun receiver() = messages

    override fun close() {
        server.close()
        clients
            .values
            .filterIsInstance<Closeable>()
            .forEach(Closeable::close)
    }
}
