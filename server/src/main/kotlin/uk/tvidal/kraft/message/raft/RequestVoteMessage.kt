package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE

class RequestVoteMessage(

    from: RaftNode,
    term: Long,

    val lastLogTerm: Long,
    val lastLogIndex: Long

) : AbstractRaftMessage(REQUEST_VOTE, from, term)
