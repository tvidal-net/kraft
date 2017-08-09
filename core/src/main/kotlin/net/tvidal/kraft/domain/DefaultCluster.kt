package net.tvidal.kraft.domain

internal class DefaultCluster(

  override val self: RaftNode,
  override val others: List<RaftNode>

) : RaftCluster {

    override val all = setOf(self, *others.toTypedArray())
    override val size = all.size

}
