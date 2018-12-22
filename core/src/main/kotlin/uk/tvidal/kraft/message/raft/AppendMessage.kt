package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.domain.RaftNode
import uk.tvidal.kraft.message.raft.RaftMessageType.APPEND
import uk.tvidal.kraft.storage.KRaftEntries

class AppendMessage(

    from: RaftNode,
    term: Long,

    val prevTerm: Long,
    val prevIndex: Long,

    val leaderCommitIndex: Long,

    val data: KRaftEntries

) : AbstractRaftMessage(APPEND, from, term)
