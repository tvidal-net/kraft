package net.tvidal.kraft

import net.tvidal.kraft.domain.DefaultCluster
import net.tvidal.kraft.domain.RaftCluster
import net.tvidal.kraft.domain.RaftNode

const val DEFAULT_CLUSTER_NAME = "RAFT"
const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L

const val BEFORE_LOG = 0L

const val LONG_BYTES = java.lang.Long.BYTES
const val INT_BYTES = java.lang.Integer.BYTES
const val SHORT_BYTES = java.lang.Short.BYTES
const val BYTE_BYTES = java.lang.Byte.BYTES

fun raftNodes(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME) = (1..size)
  .map { RaftNode(clusterName, it.toByte()) }

fun raftCluster(self: RaftNode, others: List<RaftNode>): RaftCluster = DefaultCluster(self, others)

fun raftCluster(selfNodeIndex: Int, nodes: List<RaftNode>): RaftCluster {
    val self = nodes[selfNodeIndex]
    val others = nodes.filterNot { it == self }
    return raftCluster(self, others)
}
