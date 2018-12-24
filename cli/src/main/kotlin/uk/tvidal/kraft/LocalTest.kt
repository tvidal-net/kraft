package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.transport.networkTransport
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    logbackConsoleConfiguration()

    val nodes = raftNodes(2)
    val cluster = raftCluster(nodes)
    val transport = networkTransport(nodes)
    val config = cluster.map {
        KRaftConfig(it, transport[it.self]!!, RingBufferStorage())
    }
    val server = singleThreadClusterServer(config)
    registerStopServerShutdownHook(server)
    server.start()

    thread(start = false, isDaemon = true, name = "KRaftProducerThread") {
    }
}
