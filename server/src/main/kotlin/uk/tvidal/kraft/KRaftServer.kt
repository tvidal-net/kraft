package uk.tvidal.kraft

import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.server.ClusterServer
import uk.tvidal.kraft.server.LOOP_TOLERANCE_MILLIS
import uk.tvidal.kraft.server.LoopToleranceController
import uk.tvidal.kraft.server.MultiThreadClusterServer
import uk.tvidal.kraft.server.SingleThreadClusterServer

const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val BEFORE_LOG = 0L

fun raftNodes(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME): List<RaftNode> =
    (0 until size).map { RaftNode(it, clusterName) }

fun raftCluster(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME): List<RaftCluster> =
    raftNodes(size, clusterName).let { nodes -> (0 until size).map { RaftCluster(it, nodes) } }

fun singleThreadClusterServer(
    clusterConfig: List<KRaftConfig>,
    loopToleranceMillis: Long = LOOP_TOLERANCE_MILLIS
): ClusterServer = SingleThreadClusterServer(clusterConfig, LoopToleranceController(loopToleranceMillis))

fun multiThreadClusterServer(
    clusterConfig: List<KRaftConfig>,
    loopToleranceMillis: Long = LOOP_TOLERANCE_MILLIS
): ClusterServer = MultiThreadClusterServer(clusterConfig, LoopToleranceController(loopToleranceMillis))
