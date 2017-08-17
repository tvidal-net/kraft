package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND
import net.tvidal.kraft.storage.KRaftEntries

class AppendMessage(

  override val from: RaftNode,
  override val term: Long,

  val prevTerm: Long,
  val prevIndex: Long,

  val leaderCommitIndex: Long,

  val data: KRaftEntries

) : AbstractRaftMessage(APPEND, from, term)
