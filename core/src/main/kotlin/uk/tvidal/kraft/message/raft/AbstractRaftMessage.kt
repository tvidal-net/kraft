package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractRaftMessage(
    override val type: RaftMessageType
) : AbstractMessage(), RaftMessage {

    override val headerText: String
        get() = "$type T$term ($from)"
}
