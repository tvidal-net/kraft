package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND
import net.tvidal.kraft.storage.RaftEntry
import net.tvidal.kraft.storage.RaftEntryBatch

internal class AppendMessage(

  override val source: RaftNode,
  override val term: Long,

  val prevLogTerm: Long,
  val prevLogIndex: Long,

  val leaderCommitIndex: Long,

  val entries: RaftEntryBatch<RaftEntry>

) : AbstractRaftMessage(APPEND, source, term)
