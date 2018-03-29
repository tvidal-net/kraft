package net.tvidal.kraft.tools

import joptsimple.OptionSet
import net.tvidal.kraft.Description
import net.tvidal.kraft.KRaftTool
import net.tvidal.kraft.config.KRaftConfig
import net.tvidal.kraft.config.KRaftTransportFactory
import net.tvidal.kraft.config.SizeConfig
import net.tvidal.kraft.config.TimeoutConfig
import net.tvidal.kraft.engine.RaftEngine
import net.tvidal.kraft.raftCluster
import net.tvidal.kraft.raftNodes
import net.tvidal.kraft.storage.RingBufferLogFactory
import net.tvidal.kraft.transport.KRaftTransport

@Description("Creates an in-memory two node cluster")
class TwoNodeTool : KRaftTool {

    override fun execute(op: OptionSet): Int {

        val transportFactory = object : KRaftTransportFactory {
            override fun create(): KRaftTransport {
                TODO("not implemented")
            }

        }

        val logFactory = RingBufferLogFactory(size = 0x40)

        val sizes = SizeConfig(
          maxEntrySize = 100,
          maxMessageBatchSize = 5,
          maxUnackedBytesWindow = 3000
        )

        val timeout = TimeoutConfig(
          heartbeat = 500,
          minElectionTimeout = 2000,
          maxElectionTimeout = 3000,
          firstElectionTimeout = 5000
        )

        val nodes = raftNodes(2)

        val clusters = (0 until nodes.size)
          .map { raftCluster(it, nodes) }

        val configs = clusters.map { KRaftConfig(it, timeout, transportFactory, logFactory, sizes) }

        val engines = configs.map { RaftEngine(it) }

        return 0

    }

}
