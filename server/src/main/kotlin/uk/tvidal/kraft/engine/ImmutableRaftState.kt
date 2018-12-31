package uk.tvidal.kraft.engine

import uk.tvidal.kraft.RaftCluster
import uk.tvidal.kraft.RaftNode

data class ImmutableRaftState(
    override val role: RaftRole,
    override val term: Long,
    override val leaderCommitIndex: Long,
    override val commitIndex: Long,
    override val logConsistent: Boolean,
    override val lastLogTerm: Long,
    override val lastLogIndex: Long,
    override val leader: RaftNode?,
    override val votedFor: RaftNode?,
    override val votesReceived: Set<RaftNode>,
    override val cluster: RaftCluster
) : RaftState {
    constructor(state: RaftState) : this(
        role = state.role,
        term = state.term,
        leaderCommitIndex = state.leaderCommitIndex,
        commitIndex = state.commitIndex,
        logConsistent = state.logConsistent,
        lastLogTerm = state.lastLogTerm,
        lastLogIndex = state.lastLogIndex,
        leader = state.leader,
        votedFor = state.votedFor,
        votesReceived = HashSet(state.votesReceived),
        cluster = state.cluster
    )
}
