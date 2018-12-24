package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.transport.networkTransport
import java.util.concurrent.ThreadLocalRandom
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    logbackConfigurationFile = LOGBACK_CONSOLE

    val log = KRaftLogger {}

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
        val random = ThreadLocalRandom.current()

        while (true) {
            Thread.sleep(750)
            log.info { "Publishing..." }

            val data = (1..random.nextInt(12))
                .map { "Hello World: $it".toByteArray() }

            server.publish(data)
        }

    }
}
