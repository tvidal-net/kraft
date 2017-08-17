package net.tvidal.kraft.processing

import net.tvidal.kraft.BEFORE_LOG
import net.tvidal.kraft.KRaftError
import net.tvidal.kraft.NEVER
import net.tvidal.kraft.config.KRaftConfig
import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.message.raft.AppendMessage
import org.slf4j.LoggerFactory.getLogger
import java.util.*

class RaftEngine(config: KRaftConfig) {

    private var nextElectionTime = NEVER

    private val timeout = config.timeout

    val transport = config.transport.create()
    val log = config.log.create()
    val cluster = config.cluster

    var leader: RaftNode? = null
    var votedFor: RaftNode? = null
    val votesReceived = mutableSetOf<RaftNode>()

    val self get() = cluster.self
    val others get() = cluster.others
    val singleNodeCluster get() = others.isEmpty()

    val lastLogTerm get() = log.lastLogTerm
    val lastLogIndex get() = log.lastLogIndex

    var term = 0L
    var commitIndex = 0L; private set
    var leaderCommitIndex = 0L
    var logConsistent = false

    private fun nextElectionTimeout(baseTimeout: Int) = timeout.run {
        baseTimeout + RANDOM.nextInt(maxElectionTimeout - minElectionTimeout + 1)
    }

    fun resetElectionTimeout(now: Long) {
        nextElectionTime = now + nextElectionTimeout(timeout.minElectionTimeout)
    }

    fun cancelElectionTimeout() {
        nextElectionTime = NEVER
    }

    fun updateCommitIndex(matchIndex: Long) {
        if (leaderCommitIndex > commitIndex) {
            commitIndex = minOf(leaderCommitIndex, matchIndex)
        }
    }

    fun nackIndex(msg: AppendMessage): Long {
        val leaderPrevIndex = msg.prevIndex
        return when {
            leaderPrevIndex > lastLogIndex -> lastLogIndex
            leaderPrevIndex > 0 -> leaderPrevIndex - 1
            else -> throw KRaftError("received prevIndex=$leaderPrevIndex from ${msg.from}")
        }
    }

    fun append(msg: AppendMessage): Long {

        val nextLogIndex = appendLogIndex(msg.from, msg.prevTerm, msg.prevIndex)

        return when {
            nextLogIndex > BEFORE_LOG -> log.append(msg.entries, nextLogIndex)
            else -> NEVER
        }
    }

    private fun appendLogIndex(from: RaftNode, prevTerm: Long, prevIndex: Long): Long {

        val termAtPrevIndex = log.termAt(prevIndex)
        fun log(msg: String) {
            LOG.warn("from={} log={} prevIndex={} termAtPrevIndex=[{},from={}] - {}",
              from, lastLogIndex, prevIndex, termAtPrevIndex, prevTerm, msg)
        }

        if (prevIndex == 0L || (prevIndex <= lastLogIndex && prevTerm == termAtPrevIndex)) {

            if (prevIndex < lastLogIndex) {
                if (commitIndex > prevIndex) {
                    log("CANNOT TRUNCATE BEFORE COMMIT_INDEX: $commitIndex")
                    return BEFORE_LOG
                } else {
                    log("TRUNCATE LOG")
                }
            } else {
                log("OK")
            }
            return prevIndex + 1
        }
        log("LOG IS INCONSISTENT")
        return BEFORE_LOG
    }

    val followers = object : RaftFollowers {

        val followers = others
          .map { RaftFollowerView(this@RaftEngine, it) }
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
        private val LOG = getLogger(RaftEngine::class.java)
    }
}
