package uk.tvidal.kraft

import uk.tvidal.kraft.client.consumer.LOG_HEAD
import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.storage.fileStorage
import uk.tvidal.kraft.transport.localCluster
import uk.tvidal.kraft.transport.networkTransport
import java.io.File
import java.lang.Thread.sleep
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

private val random get() = ThreadLocalRandom.current()

fun main(args: Array<String>) {
    logbackConsoleConfiguration()

    val storagePath = File("/tmp/kraft")
    if (storagePath.exists()) storagePath.deleteRecursively()
    storagePath.mkdirs()

    val nodes = raftNodes(2)
    val cluster = raftCluster(nodes)
    val transport = networkTransport(nodes)
    val config = cluster.map {
        KRaftServerConfig(it, transport[it.self]!!, fileStorage(it.self, storagePath.toPath()))
    }
    val server = singleThreadClusterServer(config)
    registerStopServerShutdownHook(server)
    server.start()

    //    /*
    val serverNode = (server.leader ?: server.randomNode).self
    val address = localCluster(nodes)[serverNode]!!

    val producer = producer(serverNode to address)
    singleThreadPool("KRaftProducerThread").loop {

        val now = Instant.now()
        val entryCount = random.nextInt(5, 51)
        val data = entries(
            (0 until entryCount).map {
                entryOf("$now Message $it ")
            }
        )

        producer.publish(data)
        sleep(random.nextInt(30, 300).toLong())
    }
    // */

    consumer(serverNode to address, index = LOG_HEAD) {
        println("received: ${it.data}")
        true
    }
}
