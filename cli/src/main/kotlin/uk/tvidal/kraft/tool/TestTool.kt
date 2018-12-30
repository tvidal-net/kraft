package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.LogConfig.appender
import uk.tvidal.kraft.LogConfig.logFile
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.SUCCESS
import uk.tvidal.kraft.client.consumer.LOG_HEAD
import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.config.TimeoutConfig
import uk.tvidal.kraft.consumer
import uk.tvidal.kraft.intArgument
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.loop
import uk.tvidal.kraft.producer
import uk.tvidal.kraft.raftCluster
import uk.tvidal.kraft.raftNodes
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.singleThreadClusterServer
import uk.tvidal.kraft.singleThreadPool
import uk.tvidal.kraft.status.StatusReporter
import uk.tvidal.kraft.storage.entries
import uk.tvidal.kraft.storage.entryOf
import uk.tvidal.kraft.storage.fileStorage
import uk.tvidal.kraft.stringArgument
import uk.tvidal.kraft.transport.NetworkTransport
import uk.tvidal.kraft.transport.config.NetworkTransportConfig
import uk.tvidal.kraft.transport.localCluster
import java.io.File
import java.lang.System.getenv
import java.lang.Thread.sleep
import java.net.InetSocketAddress
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

@Description("runs a local test cluster")
class TestTool(parser: OptionParser) : KRaftTool {

    internal companion object : KRaftLogging()

    private val opPath = parser.stringArgument("file path", "path")
        .defaultsTo(getenv("HOME") + "/kraft")

    private val opNodes = parser.intArgument("number of nodes", "nodes")
        .defaultsTo(2)

    private val opBasePort = parser.intArgument("base port", "port")
        .defaultsTo(1800)

    private fun firstElectionTimeout(node: RaftNode): Int =
        if (node.index == 1) NOW.toInt()
        else Int.MAX_VALUE

    override fun execute(op: OptionSet): Int {

        val path = File(op.valueOf(opPath))
        path.mkdirs()

        if (appender == "NOP") {
            appender = "FILE"
            logFile = path.toPath()
                .resolve("kraft.log")
                .toAbsolutePath()
                .toString()
        }

        val nodes = raftNodes(op.valueOf(opNodes))
        val localCluster = localCluster(nodes, op.valueOf(opBasePort))

        val config = raftCluster(nodes).map { cluster ->
            val transportConfig = NetworkTransportConfig(cluster.self, localCluster)
            val transport = NetworkTransport(transportConfig)
            val storage = fileStorage(cluster.self, path.toPath())
            val timeout = TimeoutConfig(
                firstElectionTimeout = firstElectionTimeout(cluster.self)
            )
            KRaftServerConfig(cluster, transport, storage, timeout)
        }

        val server = singleThreadClusterServer(config)
        registerStopServerShutdownHook(server)
        val statusReporter = StatusReporter(server)
        server.start()

        val serverNode = (server.leader ?: server.randomNode).self
        val address = localCluster[serverNode]!!
        localTest(serverNode, address)

        server.join()
        statusReporter.stop()
        return SUCCESS
    }

    private fun localTest(serverNode: RaftNode, address: InetSocketAddress) {

        var count = 0
        val producer = producer(serverNode to address)
        singleThreadPool("KRaftProducerThread").loop {

            val random = ThreadLocalRandom.current()

            val now = Instant.now()
            val entryCount = random.nextInt(1, 50)
            val data = entries(
                (0 until entryCount).map {
                    entryOf("""{"id":${++count},"time":"$now"}""")
                }
            )

            producer.publish(data)
            sleep(random.nextInt(50, 500).toLong())
        }

        consumer(serverNode to address, index = LOG_HEAD) {
            log.info { "consumer fromIndex=${it.firstIndex} ${it.data}" }
            true
        }
    }
}
