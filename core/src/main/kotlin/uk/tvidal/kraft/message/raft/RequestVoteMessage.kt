package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE

data class RequestVoteMessage(

    override val from: RaftNode,
    final override val term: Long,

    val lastLogTerm: Long,
    val lastLogIndex: Long

) : AbstractRaftMessage(REQUEST_VOTE) {

    override fun text() = "lastLogTerm=$lastLogTerm lastLogIndex=$lastLogIndex"
}
