package net.tvidal.kraft.domain

interface RaftCluster {

    val self: RaftNode

    val others: List<RaftNode>

    val all get() = setOf(self, *others.toTypedArray())

    val size get() = others.size + 1

    val majority get() = size / 2 + 1

    fun containsNode(node: RaftNode) = all.contains(node)

}
