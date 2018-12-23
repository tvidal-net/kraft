package uk.tvidal.kraft.engine

import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage

enum class RaftRole {

    FOLLOWER {
        override fun run(now: Long, raft: RaftEngine) = with(raft) {
            checkElectionTimeout(now)
        }

        override fun RaftEngine.enterRole(now: Long) {
            resetElectionTimeout(now)
        }

        override fun RaftEngine.exitRole(now: Long) {
            resetLeader()
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
        override fun RaftEngine.enterRole(now: Long) {
            startElection(now)
        }

        override fun vote(now: Long, msg: VoteMessage, raft: RaftEngine) =
            raft.processVote(msg)

        override fun append(now: Long, msg: AppendMessage, raft: RaftEngine) =
            reset()
    },

    LEADER {
        override fun run(now: Long, raft: RaftEngine): RaftRole? {
            raft.heartbeatFollowers(now)
            return null
        }

        override fun RaftEngine.enterRole(now: Long) {
            becomeLeader()
        }

        override fun RaftEngine.exitRole(now: Long) {
            resetLeader()
        }

        override fun appendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? {
            raft.processAck(msg)
            return null
        }
    },

    ERROR {
        override fun reset(): RaftRole? = null

        override fun RaftEngine.enterRole(now: Long) = cancelElectionTimeout()
    };

    @Suppress("LeakingThis")
    protected val log = KRaftLogger(this)

    protected open fun reset(): RaftRole? = FOLLOWER

    internal open fun run(now: Long, raft: RaftEngine): RaftRole? = null

    internal fun enter(now: Long, raft: RaftEngine) {
        log.info { "${raft.self} enter" }
        raft.enterRole(now)
    }

    internal open fun RaftEngine.enterRole(now: Long) {}

    internal fun exit(now: Long, raft: RaftEngine) = with(raft) {
        log.info { "${raft.self} exit" }
        resetElection()
        exitRole(now)
    }

    internal open fun RaftEngine.exitRole(now: Long) {}

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
            log.debug { "${raft.self} processMessage $msg" }
            processMessage(now, msg, raft)
        }

        // Message is from an old term, deny if it's a request vote
        msg.type == REQUEST_VOTE -> {
            log.debug { "${raft.self} dropping message due to oldTerm=${msg.term} (currentTerm=${raft.term})" }
            raft.vote(msg.from, false)
            null
        }

        else -> null
    }
}
