package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.cachedThreadPool
import uk.tvidal.kraft.codec.JsonCodecFactory
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.singleThreadPool
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

open class ClientTransportConfig(
    val node: RaftNode,
    val cluster: Map<RaftNode, InetSocketAddress>,
    val codec: SocketCodecFactory = JsonCodecFactory,
    val readerThread: ExecutorService = cachedThreadPool("$node-Reader"),
    val writerThread: ExecutorService = singleThreadPool("$node-Writer")
) {
    operator fun get(node: RaftNode) = cluster[node]!!

    val host get() = this[node]
}
