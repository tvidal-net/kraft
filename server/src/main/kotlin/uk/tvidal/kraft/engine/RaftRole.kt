package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.FOREVER
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage

enum class RaftRole {

    FOLLOWER {
        override fun enter(now: Long, raft: RaftEngine) =
            raft.resetElectionTimeout(now)

        override fun RaftEngine.exitRole(now: Long) {
            leader = null
        }

        override fun RaftEngine.append(now: Long, msg: AppendMessage): RaftRole? {
            resetElectionTimeout(now)
            leaderCommitIndex = msg.leaderCommitIndex
            val matchIndex = append(msg)

            logConsistent = matchIndex > BEFORE_LOG
            if (logConsistent) {
                updateCommitIndex(matchIndex)
                ack(msg.from, matchIndex)
            } else {
                val nackIndex = nackIndex(msg)
                nack(msg.from, nackIndex)
            }
            leader = msg.from
            return null
        }

        override fun RaftEngine.requestVote(now: Long, msg: RequestVoteMessage): RaftRole? {
            val candidate = msg.from
            val grantVote = (votedFor == null || votedFor == candidate) &&
                (lastLogTerm < msg.lastLogTerm ||
                    (lastLogTerm == msg.lastLogTerm && lastLogIndex <= msg.lastLogIndex))

            if (grantVote) {
                votedFor = candidate
                resetElectionTimeout(now)
            }
            vote(candidate, grantVote)
            return null
        }
    },

    CANDIDATE {
        override fun enter(now: Long, raft: RaftEngine) = with(raft) {
            term++
            votedFor = self
            votesReceived.clear()
            votesReceived.add(self)
            resetElectionTimeout(now)
            requestVotes()
        }

        override fun RaftEngine.vote(now: Long, msg: VoteMessage): RaftRole? {
            log.info { "vote from=${msg.from} term=${msg.term}" }
            if (msg.vote) {
                votesReceived.add(msg.from)
                val votesReceived = votesReceived.size
                if (votesReceived >= cluster.majority) {
                    return LEADER
                }
            }
            return null
        }

        override fun RaftEngine.append(now: Long, msg: AppendMessage) = reset()
    },

    LEADER {
        override fun run(now: Long, raft: RaftEngine) = raft.updateFollowers(now)

        override fun enter(now: Long, raft: RaftEngine) {
            with(raft) {
                cancelElectionTimeout()
                resetFollowers()
                leader = self
                flush()
            }
        }

        override fun RaftEngine.exitRole(now: Long) {
            leader = null
        }

        override fun RaftEngine.appendAck(now: Long, msg: AppendAckMessage): RaftRole? {
            receiveAck(msg)
            return null
        }
    },

    ERROR {
        override fun reset(): RaftRole? = null

        override fun enter(now: Long, raft: RaftEngine) = with(raft) {
            resetElectionTimeout(FOREVER)
        }
    };

    @Suppress("LeakingThis")
    protected val log = KRaftLogger(this)

    protected open fun reset(): RaftRole? = FOLLOWER

    internal open fun run(now: Long, raft: RaftEngine) {}

    internal open fun enter(now: Long, raft: RaftEngine) {}

    internal fun exit(now: Long, raft: RaftEngine) = with(raft) {
        resetElection()
        exitRole(now)
    }

    internal open fun RaftEngine.exitRole(now: Long) {}

    internal open fun RaftEngine.append(now: Long, msg: AppendMessage): RaftRole? = null

    internal open fun RaftEngine.appendAck(now: Long, msg: AppendAckMessage): RaftRole? = null

    internal open fun RaftEngine.requestVote(now: Long, msg: RequestVoteMessage): RaftRole? = null

    internal open fun RaftEngine.vote(now: Long, msg: VoteMessage): RaftRole? = null

    private fun RaftEngine.processMessage(now: Long, msg: RaftMessage) = when (msg) {
        is AppendMessage -> append(now, msg)
        is AppendAckMessage -> appendAck(now, msg)
        is RequestVoteMessage -> requestVote(now, msg)
        is VoteMessage -> vote(now, msg)
        else -> null
    }

    internal fun process(now: Long, msg: RaftMessage, raft: RaftEngine): RaftRole? = when {

        // There's a new term on the cluster, reset back to follower
        msg.term > raft.term -> {
            raft.term = msg.term
            reset()
        }

        // We're in the right term, just process it
        raft.term == msg.term -> raft.processMessage(now, msg)

        // Message is from an old term, deny if it's a request vote
        msg.type == REQUEST_VOTE -> {
            raft.vote(msg.from, false)
            null
        }
        else -> null
    }
}
