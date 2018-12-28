package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.DataMessage
import uk.tvidal.kraft.message.Payload
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND
import uk.tvidal.kraft.storage.KRaftEntries

data class AppendMessage(

    override val from: RaftNode,
    final override val term: Long,

    val prevTerm: Long,
    val prevIndex: Long,
    val leaderCommitIndex: Long,

    @Payload
    override val data: KRaftEntries

) : AbstractRaftMessage(APPEND), DataMessage {

    override fun text() = "prevIndex=$prevIndex prevTerm=$prevTerm " +
        "leaderCommitIndex=$leaderCommitIndex data=$data"

    override fun toString() = super.toString()
}
