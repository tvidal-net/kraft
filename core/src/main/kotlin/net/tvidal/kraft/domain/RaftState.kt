package net.tvidal.kraft.domain

import net.tvidal.kraft.processing.RaftRole

data class RaftState(

  val term: Long,
  val self: RaftNode,
  val leader: RaftNode,
  val role: RaftRole

) {

    val isLeader get() = self == leader

}
