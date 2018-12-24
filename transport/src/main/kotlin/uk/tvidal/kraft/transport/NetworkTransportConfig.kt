package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.cachedThreadPool
import uk.tvidal.kraft.codec.JsonCodecFactory
import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.singleThreadPool
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService

class NetworkTransportConfig(
    node: RaftNode,
    cluster: Map<RaftNode, InetSocketAddress>,
    codec: SocketCodecFactory = JsonCodecFactory,
    readerThread: ExecutorService = cachedThreadPool("$node-Reader"),
    writerThread: ExecutorService = singleThreadPool("$node-Writer"),
    val serverThread: ExecutorService = singleThreadPool("$node-Server"),
    val messageReceiver: MessageReceiver = DualQueueMessageReceiver()
) : ClientTransportConfig(node, cluster, codec, readerThread, writerThread) {

    val others = cluster.keys.filter { it != node }
}
