package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.config.TimeoutConfig
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.fileStorage
import uk.tvidal.kraft.transport.NetworkTransport
import uk.tvidal.kraft.transport.config.NetworkTransportConfig
import uk.tvidal.kraft.transport.localCluster
import java.io.File
import java.util.concurrent.ThreadLocalRandom

object LocalTest {

    private val random get() = ThreadLocalRandom.current()

    private const val NODES = 3
    private const val LEADER = 1
    private const val PATH = "/tmp/kraft"

    private fun firstElectionTimeout(node: RaftNode): Int = when (node.index) {
        LEADER -> NOW.toInt()
        else -> Int.MAX_VALUE
    }

    @JvmStatic
    fun main(vararg args: String) {
        val storagePath = File(PATH)
        storagePath.mkdirs()

        val nodes = raftNodes(NODES)
        val localCluster = localCluster(nodes)
        val config = raftCluster(nodes).map { cluster ->
            val transportConfig = NetworkTransportConfig(cluster.self, localCluster)
            val transport = NetworkTransport(transportConfig)
            val storage = fileStorage(cluster.self, storagePath.toPath())
            val timeout = TimeoutConfig(
                firstElectionTimeout = firstElectionTimeout(cluster.self)
            )
            KRaftServerConfig(cluster, transport, storage, timeout)
        }
        val server = singleThreadClusterServer(config)
        registerStopServerShutdownHook(server)
        server.start()
        server.join()

        /*
       val serverNode = (server.leader ?: server.randomNode).self
       val address = localCluster[serverNode]!!

       val producer = producer(serverNode to address)
       singleThreadPool("KRaftProducerThread").loop {

           val now = Instant.now()
           val entryCount = random.nextInt(1, 50)
           val data = entries(
               (0 until entryCount).map {
                   entryOf("$now Message $it ")
               }
           )

           producer.publish(data)
           sleep(random.nextInt(300, 500).toLong())
       }

       consumer(serverNode to address, index = LOG_HEAD) {
           log.info { ORANGE.format("consumer fromIndex=${it.firstIndex} ${it.data}") }
           true
       }
       // */
    }
}
