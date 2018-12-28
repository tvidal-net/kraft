package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.DataMessage
import uk.tvidal.kraft.message.Payload
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND
import uk.tvidal.kraft.storage.KRaftEntries

class AppendMessage(

    from: RaftNode,
    term: Long,

    val prevTerm: Long,
    val prevIndex: Long,

    val leaderCommitIndex: Long,

    @Payload
    override val data: KRaftEntries

) : AbstractRaftMessage(APPEND, from, term), DataMessage {

    override fun text() = "prevIndex=$prevIndex prevTerm=$prevTerm " +
        "leaderCommitIndex=$leaderCommitIndex data=$data"
}
