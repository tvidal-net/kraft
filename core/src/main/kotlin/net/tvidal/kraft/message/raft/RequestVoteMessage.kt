package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE

internal class RequestVoteMessage(

  override val source: RaftNode,
  override val term: Long,

  val lastLogTerm: Long,
  val lastLogIndex: Long

) : AbstractRaftMessage(REQUEST_VOTE, source, term)
