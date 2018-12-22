package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.transport.LocalTransportFactory

fun main(args: Array<String>) {
    logbackConfigurationFile = LOGBACK_CONSOLE

    val transport = LocalTransportFactory()
    val cluster = raftCluster(2)
    val config = cluster.map {
        KRaftConfig(it, transport.create(), RingBufferStorage())
    }
    val server = raftClusterServer(config)
    registerStopServerShutdownHook(server)
    server.start()
}
