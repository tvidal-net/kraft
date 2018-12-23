package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.cachedThreadPool
import uk.tvidal.kraft.codec.JsonCodecFactory
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.singleThreadPool
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

class NetworkTransportConfig(
    val node: RaftNode,
    val cluster: Map<RaftNode, InetSocketAddress>,
    val codec: SocketCodecFactory = JsonCodecFactory,
    val messageReceiver: MessageReceiver = DualQueueMessageReceiver(),
    val serverThread: ExecutorService = singleThreadPool("$node-Server"),
    val readerThread: ExecutorService = cachedThreadPool("$node-Reader"),
    val writerThread: ExecutorService = singleThreadPool("$node-Writer")
) {
    val host get() = this[node]

    operator fun get(node: RaftNode) = cluster[node]!!

    val others = cluster.keys.filter { it != node }
}
