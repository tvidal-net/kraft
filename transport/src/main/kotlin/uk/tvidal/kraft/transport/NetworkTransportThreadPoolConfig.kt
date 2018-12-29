package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.cachedThreadPool
import uk.tvidal.kraft.singleThreadPool
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService

class NetworkTransportThreadPoolConfig(
    val self: RaftNode,
    val writerThread: ScheduledExecutorService = networkWriterThread,
    val readerThread: ExecutorService = cachedThreadPool("$self-Reader"),
    val serverThread: ExecutorService = singleThreadPool("$self-Server")
)
