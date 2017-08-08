package net.tvidal.kraft.domain

internal class DefaultRaftCluster(

  private val self: RaftNode,
  private val others: List<RaftNode>

) : RaftCluster {

    private val all = listOf(self(), *others().toTypedArray())
    private val size = all.size

    override fun self() = self
    override fun others() = others
    override fun all() = all
    override fun size() = size

}
