package net.tvidal.kraft.message.raft

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessageType.VOTE

class VoteMessage(

    override val from: RaftNode,
    override val term: Long,

    val vote: Boolean

) : AbstractRaftMessage(VOTE, from, term)
