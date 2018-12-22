package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractRaftMessage(

    final override val type: RaftMessageType,
    from: RaftNode,
    final override val term: Long

) : AbstractMessage(type, from), RaftMessage {

    override fun toString() = "$type:$term (from: $from)"
}
