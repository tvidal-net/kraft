package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND_ACK

class AppendAckMessage(

    from: RaftNode,
    term: Long,

    val ack: Boolean,
    val matchIndex: Long

) : AbstractRaftMessage(APPEND_ACK, from, term)
