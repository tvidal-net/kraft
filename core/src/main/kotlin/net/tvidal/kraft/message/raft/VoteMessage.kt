package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.VOTE

internal class VoteMessage(

  override val source: RaftNode,
  override val term: Long,

  val vote: Boolean

) : AbstractRaftMessage(VOTE, source, term)
