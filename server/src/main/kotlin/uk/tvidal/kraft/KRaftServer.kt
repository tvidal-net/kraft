package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.server.KRaftServer
import uk.tvidal.kraft.server.SingleThreadClusterServer

const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val BEFORE_LOG = 0L

fun raftNodes(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME): List<RaftNode> =
    (1..size).map { RaftNode(it.toByte(), clusterName) }

fun raftCluster(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME): List<RaftCluster> =
    raftNodes(size, clusterName).let { nodes -> (1..size).map { RaftCluster(it, nodes) } }

fun raftClusterServer(clusterConfig: List<KRaftConfig>): KRaftServer = SingleThreadClusterServer(clusterConfig)
