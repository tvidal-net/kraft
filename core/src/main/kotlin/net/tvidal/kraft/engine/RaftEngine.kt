package net.tvidal.kraft.engine

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

    private val timeout = config.timeout
    private var nextElectionTime = NEVER

    internal val transport = config.transportFactory.create()
    internal val log = config.logFactory.create()
    internal val sizes = config.sizes

    val cluster = config.cluster
    var leader: RaftNode? = null; internal set
    var votedFor: RaftNode? = null; internal set
    val votesReceived = mutableSetOf<RaftNode>()

    val self get() = cluster.self
    val others get() = cluster.others
    val singleNodeCluster get() = others.isEmpty()

    val lastLogTerm get() = log.lastLogTerm
    val lastLogIndex get() = log.lastLogIndex

    var term = 0L; internal set
    var commitIndex = 0L; private set
    var leaderCommitIndex = 0L; internal set
    var logConsistent = false; internal set

    val heartbeatWindowMillis get() = timeout.heartbeat

    val followers = Followers()

    private fun nextElectionTimeout(baseTimeout: Int = timeout.minElectionTimeout) = timeout.run {
        baseTimeout + RANDOM.nextInt(maxElectionTimeout - minElectionTimeout + 1)
    }

    internal fun resetElectionTimeout(now: Long) {
        nextElectionTime = now + nextElectionTimeout()
    }

    internal fun cancelElectionTimeout() {
        nextElectionTime = NEVER
    }

    internal fun updateCommitIndex(matchIndex: Long) {
        if (leaderCommitIndex > commitIndex) {
            commitIndex = minOf(leaderCommitIndex, matchIndex)
        }
    }

    internal fun nackIndex(msg: AppendMessage): Long {
        val leaderPrevIndex = msg.prevIndex
        return when {
            leaderPrevIndex > lastLogIndex -> lastLogIndex
            leaderPrevIndex > 0 -> leaderPrevIndex - 1
            else -> throw KRaftError("received prevIndex=$leaderPrevIndex from ${msg.from}")
        }
    }

    internal fun append(msg: AppendMessage) = appendLogIndex(msg).let {
        when {
            it > BEFORE_LOG -> log.append(msg.data, it)
            else -> NEVER
        }
    }

    private fun appendLogIndex(msg: AppendMessage): Long {
        val prevTerm = msg.prevTerm
        val prevIndex = msg.prevIndex
        val termAtPrevIndex = log.termAt(prevIndex)

        val logMessage = "from={} log={} prevIndex={} termAtPrevIndex=[{},from={}] - {}"
        val logData = arrayOf(msg.from, lastLogIndex, prevIndex, termAtPrevIndex, prevTerm)

        if (prevIndex == 0L || (prevIndex <= lastLogIndex && prevTerm == termAtPrevIndex)) {

            if (prevIndex < lastLogIndex) {
                if (commitIndex > prevIndex) {
                    LOG.warn(logMessage, *logData, "CANNOT TRUNCATE BEFORE COMMIT_INDEX: $commitIndex")
                    return BEFORE_LOG
                } else {
                    LOG.info(logMessage, *logData, "TRUNCATE LOG")
                }
            } else {
                LOG.debug(logMessage, *logData, "OK")
            }
            return prevIndex + 1
        }
        LOG.warn(logMessage, *logData, "LOG IS INCONSISTENT")
        return BEFORE_LOG
    }

    inner class Followers {

        private val followers = others
          .map { RaftFollowerState(this@RaftEngine, transport.sender(it)) }
          .associateBy { it.follower }

        fun reset() {
            followers.values.forEach(RaftFollowerState::reset)
        }

        fun run(now: Long) {
            followers.values.forEach { it.run(now) }
        }

        fun ack(msg: AppendAckMessage) {
            val follower = followers[msg.from]
            if (follower != null) follower.ack(msg)
            else LOG.error("There is no state for follower {}", msg.from)
        }

        fun updateCommitIndex() {
            val matchIndex = followers.values.map(RaftFollowerState::matchIndex) + lastLogIndex
            val quorumCommitIndex = matchIndex.sorted().take(cluster.majority).last()

            if (quorumCommitIndex > commitIndex) {

                val quorumCommitTerm = log.termAt(quorumCommitIndex)
                if (quorumCommitTerm == term) {
                    LOG.info("updateCommitIndex={} from={}", quorumCommitIndex, commitIndex)
                    updateCommitIndex(quorumCommitIndex)
                    followers.values.forEach(RaftFollowerState::commit)

                } else {
                    LOG.warn("SKIPPING updateCommitIndex={} quorumCommitTerm={} currentTerm={}",
                      quorumCommitIndex, quorumCommitTerm, term)
                }
            }
        }

    }

    companion object {
        private val RANDOM = Random()
        private val LOG = getLogger(RaftEngine::class.java)
    }
}