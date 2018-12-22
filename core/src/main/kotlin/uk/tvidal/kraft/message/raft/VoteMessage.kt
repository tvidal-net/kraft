package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.VOTE

class VoteMessage(

    from: RaftNode,
    term: Long,

    val vote: Boolean

) : AbstractRaftMessage(VOTE, from, term)
