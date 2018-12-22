package net.tvidal.kraft.domain

data class RaftCluster(
    val self: RaftNode,
    val others: List<RaftNode> = emptyList()
) {
    constructor(self: Int, nodes: List<RaftNode>) : this(
        self = nodes[self],
        others = nodes.filterIndexed { i, _ -> i != self }
    )

    constructor(self: Int, vararg nodes: RaftNode) :
        this(self, nodes.toList())

    private val nodes: Set<RaftNode> = setOf(self, *others.toTypedArray())

    val size: Int
        get() = nodes.size

    val single: Boolean
        get() = others.isEmpty()

    val majority: Int
        get() = size / 2 + 1

    fun contains(node: RaftNode) = node in nodes
}
