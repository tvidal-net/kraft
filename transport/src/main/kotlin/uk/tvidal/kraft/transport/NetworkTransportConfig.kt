package uk.tvidal.kraft.transport

import uk.tvidal.kraft.HEARTBEAT_TIMEOUT
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.codec.ProtoCodecFactory
import uk.tvidal.kraft.codec.SocketCodecFactory
import java.net.InetSocketAddress

class NetworkTransportConfig(
    val self: RaftNode,
    val cluster: Map<RaftNode, InetSocketAddress>,
    val heartBeatTimeout: Int = HEARTBEAT_TIMEOUT,
    val codec: SocketCodecFactory = ProtoCodecFactory,
    val messageReceiver: MessageReceiver = DualQueueMessageReceiver(),
    val threadPoolConfig: NetworkTransportThreadPoolConfig = NetworkTransportThreadPoolConfig(self)
) {
    operator fun get(node: RaftNode) = cluster[node]!!

    val host get() = this[self]

    val others = cluster.keys.filter { it != self }

    val writerThread get() = threadPoolConfig.writerThread
    val readerThread get() = threadPoolConfig.readerThread
    val serverThread get() = threadPoolConfig.serverThread
}
