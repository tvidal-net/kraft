package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE

class RequestVoteMessage(

    override val from: RaftNode,
    override val term: Long,

    val lastLogTerm: Long,
    val lastLogIndex: Long

) : AbstractRaftMessage(REQUEST_VOTE, from, term)
