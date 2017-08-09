package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.*
import net.tvidal.kraft.storage.KRaftEntryBatch

class AppendMessage(

  override val from: RaftNode,
  override val term: Long,

  val prevTerm: Long,
  val prevIndex: Long,

  val leaderCommitIndex: Long,

  val entries: KRaftEntryBatch

) : AbstractRaftMessage(APPEND, from, term)
