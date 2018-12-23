package uk.tvidal.kraft.engine

import uk.tvidal.kraft.RaftCluster
import uk.tvidal.kraft.RaftNode

interface RaftState {

    val role: RaftRole
    val term: Long

    val commitIndex: Long
    val leaderCommitIndex: Long

    val logConsistent: Boolean
    val lastLogTerm: Long
    val lastLogIndex: Long
    val nextLogIndex: Long

    val leader: RaftNode?
    val votedFor: RaftNode?
    val votesReceived: Set<RaftNode>

    val cluster: RaftCluster

    val self: RaftNode
        get() = cluster.self

    val others: Collection<RaftNode>
        get() = cluster.others

    val isSingleNodeCluster: Boolean
        get() = cluster.single
}
