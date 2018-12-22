package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.VOTE

class VoteMessage(

    from: RaftNode,
    term: Long,

    val vote: Boolean

) : AbstractRaftMessage(VOTE, from, term)
