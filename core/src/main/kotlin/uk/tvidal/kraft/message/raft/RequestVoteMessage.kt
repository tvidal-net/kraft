package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE

class RequestVoteMessage(

    from: RaftNode,
    term: Long,

    val lastLogTerm: Long,
    val lastLogIndex: Long

) : AbstractRaftMessage(REQUEST_VOTE, from, term)
