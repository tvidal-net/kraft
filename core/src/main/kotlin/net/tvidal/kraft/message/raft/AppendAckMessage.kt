package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND_ACK

internal class AppendAckMessage(

  override val source: RaftNode,
  override val term: Long,

  val ack: Boolean,
  val matchIndex: Long

) : AbstractRaftMessage(APPEND_ACK, source, term)
