package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND_ACK

class AppendAckMessage(

    from: RaftNode,
    term: Long,

    val ack: Boolean,
    val matchIndex: Long

) : AbstractRaftMessage(APPEND_ACK, from, term)
