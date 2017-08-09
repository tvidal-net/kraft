package net.tvidal.kraft.domain

import net.tvidal.kraft.processing.RaftRole
import net.tvidal.kraft.processing.RaftRole.*

data class RaftState(

  var role: RaftRole = FOLLOWER,
  var term: Long = 0L,
  var leader: RaftNode? = null

)
