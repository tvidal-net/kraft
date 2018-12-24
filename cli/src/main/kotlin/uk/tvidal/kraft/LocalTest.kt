package uk.tvidal.kraft

import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.RingBufferStorage
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.transport.localCluster
import uk.tvidal.kraft.transport.messageSender
import uk.tvidal.kraft.transport.networkTransport
import java.lang.Thread.sleep
import java.util.concurrent.ThreadLocalRandom

private val random get() = ThreadLocalRandom.current()

fun main(args: Array<String>) {
    logbackConsoleConfiguration()

    val nodes = raftNodes(1)
    val cluster = raftCluster(nodes)
    val transport = networkTransport(nodes)
    val config = cluster.map {
        KRaftConfig(it, transport[it.self]!!, RingBufferStorage())
    }
    val server = singleThreadClusterServer(config)
    registerStopServerShutdownHook(server)
    server.start()

    //    /*
    val producerNode = clientNode("Producer")
    val serverNode = (server.leader ?: server.randomNode).self
    val address = localCluster(nodes)[serverNode]!!

    val producer = producer(
        sender = messageSender(serverNode to address, producerNode)
    )
    singleThreadPool("KRaftProducerThread").loop {

        val entryCount = random.nextInt(5, 51)
        val data = entries(
            (0 until entryCount).map {
                entryOf("Message $it")
            }
        )

        producer.publish(data)
        sleep(random.nextInt(30, 300).toLong())
    }
    // */
}
