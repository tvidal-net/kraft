package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode

internal abstract class AbstractRaftMessage(

  override val type: RaftMessageType,
  override val source: RaftNode,
  override val term: Long

) : RaftMessage
