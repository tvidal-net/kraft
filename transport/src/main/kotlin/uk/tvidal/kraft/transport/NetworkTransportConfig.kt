package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.cachedThreadPool
import uk.tvidal.kraft.codec.JsonCodecFactory
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.singleThreadPool
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

class NetworkTransportConfig(
    val self: RaftNode,
    val cluster: Map<RaftNode, InetSocketAddress>,
    val codec: SocketCodecFactory = JsonCodecFactory,
    val readerThread: ExecutorService = cachedThreadPool("$self-Reader"),
    val writerThread: ExecutorService = singleThreadPool("$self-Writer"),
    val serverThread: ExecutorService = singleThreadPool("$self-Server"),
    val messageReceiver: MessageReceiver = DualQueueMessageReceiver()
) {
    operator fun get(node: RaftNode) = cluster[node]!!

    val host get() = this[self]

    val others = cluster.keys.filter { it != self }
}
