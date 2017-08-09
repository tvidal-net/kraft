package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND
import net.tvidal.kraft.storage.KRaftEntry
import net.tvidal.kraft.storage.KRaftEntryBatch

class AppendMessage(

  override val from: RaftNode,
  override val term: Long,

  val prevLogTerm: Long,
  val prevLogIndex: Long,

  val leaderCommitIndex: Long,

  val entries: KRaftEntryBatch<KRaftEntry>

) : AbstractRaftMessage(APPEND, from, term)
