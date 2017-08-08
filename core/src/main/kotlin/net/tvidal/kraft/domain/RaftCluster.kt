package net.tvidal.kraft.domain

interface RaftCluster {

    fun self(): RaftNode

    fun others(): List<RaftNode>

    fun all() = listOf(self(), *others().toTypedArray())

    fun size(): Int = others().size + 1

    fun majority() = size() / 2 + 1

}
