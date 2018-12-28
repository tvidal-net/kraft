package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND_ACK

data class AppendAckMessage(

    override val from: RaftNode,
    final override val term: Long,

    val ack: Boolean,
    val matchIndex: Long

) : AbstractRaftMessage(APPEND_ACK) {

    override fun text() = "ack=$ack matchIndex=$matchIndex"
}
