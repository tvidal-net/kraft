package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.transport.LocalTransportFactory
import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom

fun main(args: Array<String>) {
    logbackConfigurationFile = LOGBACK_CONSOLE

    val log = KRaftLogger {}

    val transport = LocalTransportFactory()
    val cluster = raftCluster(5)
    val config = cluster.map {
        KRaftConfig(it, transport.create(), RingBufferStorage())
    }
    val server = multiThreadClusterServer(config)
    registerStopServerShutdownHook(server)
    server.start()

    Thread(
        Runnable {
            val random = ThreadLocalRandom.current()

            while (true) {
                sleep(750)
                log.info { "Publishing..." }

                val data = (1..random.nextInt(12))
                    .map { "Hello World: $it".toByteArray() }

                server.publish(data)
            }
        },
        "KRaftProducerThread"
    ).apply {
        isDaemon = true
        start()
    }
}
