package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.AbstractMessage

abstract class AbstractRaftMessage(

    final override val type: RaftMessageType,
    from: RaftNode,
    final override val term: Long

) : AbstractMessage(type, from), RaftMessage {

    override fun toString() = "$type:$term (from: $from)"
}
