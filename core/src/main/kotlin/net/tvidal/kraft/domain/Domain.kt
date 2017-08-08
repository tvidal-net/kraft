package net.tvidal.kraft.domain

const val CLUSTER_NAME = "RAFT"

fun raftNodes(size: Int, clusterName: String = CLUSTER_NAME) = (1..size)
  .map { RaftNode(clusterName, it.toByte()) }

fun raftCluster(self: RaftNode, others: List<RaftNode>): RaftCluster = DefaultRaftCluster(self, others)

fun raftCluster(selfNodeIndex: Int, nodes: List<RaftNode>): RaftCluster {
    val self = nodes[selfNodeIndex]
    val others = nodes.filterNot { it == self }
    return raftCluster(self, others)
}

