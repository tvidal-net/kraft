package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractRaftMessage(
    override val type: RaftMessageType,
    override val from: RaftNode,
    override val term: Long
) : AbstractMessage(type, from), RaftMessage {

    override val headerText: String
        get() = "$type T$term ($from)"
}
