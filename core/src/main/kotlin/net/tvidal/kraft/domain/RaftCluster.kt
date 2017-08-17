package net.tvidal.kraft.domain

interface RaftCluster {

    val self: RaftNode
    val others: List<RaftNode>
    val majority: Int

    fun contains(node: RaftNode): Boolean

}
