package uk.tvidal.kraft.engine

import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage

enum class RaftRole {

    FOLLOWER {
        override fun work(now: Long, raft: RaftEngine): RaftRole? =
            raft.checkElectionTimeout(now)

        override fun enterRole(now: Long, raft: RaftEngine) {
            raft.resetElectionTimeout(now)
        }

        override fun exitRole(now: Long, raft: RaftEngine) {
            raft.resetLeader()
        }

        override fun append(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? {
            raft.appendEntries(now, msg)
            return null
        }

        override fun requestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? {
            raft.processRequestVote(now, msg)
            return null
        }
    },

    CANDIDATE {
        override fun work(now: Long, raft: RaftEngine): RaftRole? =
            raft.checkElectionTimeout(now)

        override fun enterRole(now: Long, raft: RaftEngine) {
            raft.startElection(now)
        }

        override fun requestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? =
            reset()

        override fun vote(now: Long, msg: VoteMessage, raft: RaftEngine) = raft.processVote(msg)

        override fun append(now: Long, msg: AppendMessage, raft: RaftEngine) = reset()
    },

    LEADER {
        override fun work(now: Long, raft: RaftEngine): RaftRole? {
            raft.heartbeatFollowers(now)
            return null
        }

        override fun enterRole(now: Long, raft: RaftEngine) {
            raft.becomeLeader()
        }

        override fun exitRole(now: Long, raft: RaftEngine) {
            raft.resetLeader()
        }

        override fun appendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? {
            raft.processAck(msg)
            return null
        }
    },

    ERROR {
        override fun reset(): RaftRole? = null

        override fun enterRole(now: Long, raft: RaftEngine) {
            raft.cancelElectionTimeout()
        }
    };

    @Suppress("LeakingThis")
    protected val log = KRaftLogger(this)

    protected open fun reset(): RaftRole? = FOLLOWER

    internal open fun work(now: Long, raft: RaftEngine): RaftRole? = null

    internal fun enter(now: Long, raft: RaftEngine) {
        log.info { "[${raft.self}] Enter $name T${raft.term}" }
        enterRole(now, raft)
    }

    internal open fun enterRole(now: Long, raft: RaftEngine) {}

    internal fun exit(now: Long, raft: RaftEngine) {
        raft.resetElection()
        exitRole(now, raft)
    }

    internal open fun exitRole(now: Long, raft: RaftEngine) {}

    internal open fun append(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? = null

    internal open fun appendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? = null

    internal open fun requestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? = null

    internal open fun vote(now: Long, msg: VoteMessage, raft: RaftEngine): RaftRole? = null

    private fun processMessage(now: Long, msg: RaftMessage, raft: RaftEngine): RaftRole? = when (msg) {
        is AppendMessage -> append(now, msg, raft)
        is AppendAckMessage -> appendAck(now, msg, raft)
        is RequestVoteMessage -> requestVote(now, msg, raft)
        is VoteMessage -> vote(now, msg, raft)
        else -> null
    }

    internal fun process(now: Long, msg: RaftMessage, raft: RaftEngine): RaftRole? = when {

        // There's a new term on the cluster, reset back to follower
        msg.term > raft.term -> {
            raft.updateTerm(msg.term)
            reset()
        }

        // We're in the right term, just process it
        raft.term == msg.term -> {
            log.trace { "[${raft.self}] process $msg" }
            processMessage(now, msg, raft)
        }

        else -> null
    }
}
