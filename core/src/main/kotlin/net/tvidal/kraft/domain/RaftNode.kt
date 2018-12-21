package net.tvidal.kraft.domain

import net.tvidal.kraft.DEFAULT_CLUSTER_NAME

data class RaftNode(
    val nodeIndex: Byte,
    val clusterName: String = DEFAULT_CLUSTER_NAME
) {
    companion object {
        val EMPTY = RaftNode(0, "NONE")
    }

    override fun toString() = "$clusterName:$nodeIndex"
}
