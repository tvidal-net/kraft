package net.tvidal.kraft

import net.tvidal.kraft.domain.RaftCluster
import net.tvidal.kraft.domain.RaftNode

const val DEFAULT_CLUSTER_NAME = "KRAFT"
const val FOREVER = 253402300799999L // 9999-12-31 23:59:59.999
const val NEVER = -1L
const val NOW = 0L

const val BEFORE_LOG = 0L

const val LONG_BYTES = java.lang.Long.BYTES
const val INT_BYTES = java.lang.Integer.BYTES
const val SHORT_BYTES = java.lang.Short.BYTES
const val BYTE_BYTES = java.lang.Byte.BYTES

fun raftMajority(clusterSize: Int) = clusterSize / 2 + 1

fun raftNodes(size: Int, clusterName: String = DEFAULT_CLUSTER_NAME) = (1..size)
  .map { RaftNode(clusterName, it.toByte()) }

fun raftCluster(self: RaftNode, others: List<RaftNode>) = object : RaftCluster {
    private val all = setOf(self, *others.toTypedArray())
    override val self = self
    override val others = others
    override val majority = raftMajority(all.size)
    override fun contains(node: RaftNode) = all.contains(node)
}

fun raftCluster(selfNodeIndex: Int, nodes: List<RaftNode>) = object : RaftCluster {
    private val all = nodes.toSet()
    override val self = nodes[selfNodeIndex]
    override val others = nodes.filterNot { it == self }.toList()
    override val majority = raftMajority(all.size)
    override fun contains(node: RaftNode) = all.contains(node)
}
