package uk.tvidal.kraft.tool

import joptsimple.OptionParser
import joptsimple.OptionSet
import uk.tvidal.kraft.Description
import uk.tvidal.kraft.KRaftTool
import uk.tvidal.kraft.LogConfig.appender
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.SUCCESS
import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.config.TimeoutConfig
import uk.tvidal.kraft.intArgument
import uk.tvidal.kraft.raftCluster
import uk.tvidal.kraft.raftNodes
import uk.tvidal.kraft.server.registerStopServerShutdownHook
import uk.tvidal.kraft.singleThreadClusterServer
import uk.tvidal.kraft.storage.fileStorage
import uk.tvidal.kraft.stringArgument
import uk.tvidal.kraft.transport.NetworkTransport
import uk.tvidal.kraft.transport.config.NetworkTransportConfig
import uk.tvidal.kraft.transport.localCluster
import java.io.File
import java.lang.System.getProperty

@Description("runs a local test cluster")
class TestTool(parser: OptionParser) : KRaftTool {

    private val opPath = parser.stringArgument("file path", "path")
        .defaultsTo(getProperty("user.dir"))

    private val opNodes = parser.intArgument("number of nodes", "nodes")
        .defaultsTo(2)

    private val opBasePort = parser.intArgument("base port", "port")
        .defaultsTo(1800)

    private fun firstElectionTimeout(node: RaftNode): Int =
        if (node.index == 1) NOW.toInt()
        else Int.MAX_VALUE

    override fun execute(op: OptionSet): Int {
        appender = "STDOUT"
        val nodes = raftNodes(op.valueOf(opNodes))
        val localCluster = localCluster(nodes, op.valueOf(opBasePort))

        val path = File(op.valueOf(opPath))
        path.mkdirs()

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
        with(server) {
            start()
            join()
        }
        return SUCCESS
    }
}
