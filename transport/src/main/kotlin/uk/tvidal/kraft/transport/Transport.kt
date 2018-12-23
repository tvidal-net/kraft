package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message
import java.net.InetSocketAddress

typealias SocketMessageReader = Iterable<Message?>
typealias SocketMessageWriter = (Message) -> Unit

const val BASE_PORT = 1800

fun localCluster(nodes: Collection<RaftNode>, basePort: Int = BASE_PORT): Map<RaftNode, InetSocketAddress> = nodes.associate {
    it to InetSocketAddress(basePort + it.index)
}

fun networkTransportConfig(cluster: Map<RaftNode, InetSocketAddress>): List<NetworkTransportConfig> = cluster.map { (node, _) ->
    NetworkTransportConfig(node, cluster)
}

fun networkTransport(nodes: Collection<RaftNode>, basePort: Int = BASE_PORT) =
    networkTransport(networkTransportConfig(localCluster(nodes, basePort)))

fun networkTransport(configs: Collection<NetworkTransportConfig>): Map<RaftNode, NetworkTransport> = configs.associate {
    it.node to NetworkTransport(it)
}
