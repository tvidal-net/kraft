package net.tvidal.kraft.processing

import net.tvidal.kraft.NO_ELECTION
import net.tvidal.kraft.KRaftError
import net.tvidal.kraft.config.KRaftConfig
import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.domain.RaftState
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.message.raft.AppendMessage
import java.util.*

class RaftEngine(config: KRaftConfig) {

    private var nextElectionTime = -1L

    val transport = config.transport.create()
    val log = config.log.create()
    val timeout = config.timeout
    val cluster = config.cluster
    val state = RaftState()
    var votedFor: RaftNode? = null
    val votesReceived = mutableSetOf<RaftNode>()
    val self; get() = cluster.self
    val others; get() = cluster.others
    val singleNodeCluster; get() = others.isEmpty()
    var commitIndex = 0L; private set
    var leaderCommitIndex = 0L
    var logConsistent = false

    var term; get() = state.term; set(value) {
        state.term = value
    }

    var leader; get() = state.leader; set(value) {
        state.leader = value
    }

    private fun nextElectionTimeout(baseTimeout: Int) = timeout.run {
        baseTimeout + RANDOM.nextInt(maxElectionTimeout - minElectionTimeout + 1)
    }

    fun resetElectionTimeout(now: Long) {
        nextElectionTime = now + nextElectionTimeout(timeout.minElectionTimeout)
    }

    fun cancelElectionTimeout() {
        nextElectionTime = NO_ELECTION
    }

    fun updateCommitIndex(matchIndex: Long) {
        if (leaderCommitIndex > commitIndex) {
            commitIndex = minOf(leaderCommitIndex, matchIndex)
        }
    }

    fun nackIndex(msg: AppendMessage): Long {
        val leaderPrevIndex = msg.prevLogIndex
        val lastLogIndex = log.lastLogIndex
        val nackIndex = when {
            leaderPrevIndex > lastLogIndex -> lastLogIndex
            leaderPrevIndex > 0 -> leaderPrevIndex - 1
            else -> throw KRaftError("received prevIndex=$leaderPrevIndex from ${msg.from}")
        }
        return nackIndex
    }

    val followers = object : RaftFollowers {

        val followers = others
          .map { RaftFollowerView(it) }
          .associateBy { it.follower }

        override fun reset() {
            followers.values.forEach { it.reset() }
        }

        override fun work(now: Long) {
            followers.values.forEach { it.work(now) }
        }

        override fun handleAck(msg: AppendAckMessage) {
            followers[msg.from]?.handleAck(msg)
        }

        override fun updateCommitIndex() {

        }

    }

    companion object {
        private val RANDOM = Random()
    }
}
