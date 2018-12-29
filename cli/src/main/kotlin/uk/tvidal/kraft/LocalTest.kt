package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.config.TimeoutConfig
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.storage.fileStorage
import uk.tvidal.kraft.transport.NetworkTransport
import uk.tvidal.kraft.transport.config.NetworkTransportConfig
import uk.tvidal.kraft.transport.localCluster
import java.io.File
import java.util.concurrent.ThreadLocalRandom

private val random get() = ThreadLocalRandom.current()

const val NODES = 3
const val LEADER = 1
const val PATH = "/tmp/kraft"

private fun firstElectionTimeout(node: RaftNode): Int = when (node.index) {
    LEADER -> NOW
    else -> NEVER
}.toInt()

fun main(args: Array<String>) {
    logbackConsoleConfiguration()
    val log = KRaftLogger {}

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

    /*
val serverNode = (server.leader ?: server.randomNode).self
val address = localCluster[serverNode]!!

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
    sleep(random.nextInt(30, 50).toLong())
}

consumer(serverNode to address, index = LOG_HEAD) {
    log.info { ORANGE.format("consumer fromIndex=${it.firstIndex} ${it.data}") }
    true
}
// */
}
