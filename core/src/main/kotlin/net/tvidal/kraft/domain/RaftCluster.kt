package net.tvidal.kraft.domain

interface RaftCluster {

    val self: RaftNode

    val others: List<RaftNode>

    val all get() = listOf(self, *others.toTypedArray())

    val size get() = others.size + 1

    val majority get() = size / 2 + 1

}
