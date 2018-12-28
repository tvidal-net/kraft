package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.VOTE

data class VoteMessage(

    override val from: RaftNode,
    final override val term: Long,

    val vote: Boolean

) : AbstractRaftMessage(VOTE) {

    override fun text() = "vote=$vote"
}
