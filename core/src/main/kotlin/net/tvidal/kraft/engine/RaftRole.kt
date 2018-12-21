package net.tvidal.kraft.engine

import net.tvidal.kraft.BEFORE_LOG
import net.tvidal.kraft.FOREVER
import net.tvidal.kraft.message.client.ClientAppendMessage
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.message.raft.AppendMessage
import net.tvidal.kraft.message.raft.RaftMessage
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND
import net.tvidal.kraft.message.raft.RaftMessageType.APPEND_ACK
import net.tvidal.kraft.message.raft.RaftMessageType.REQUEST_VOTE
import net.tvidal.kraft.message.raft.RaftMessageType.VOTE
import net.tvidal.kraft.message.raft.RequestVoteMessage
import net.tvidal.kraft.message.raft.VoteMessage
import net.tvidal.kraft.storage.flush
import org.slf4j.LoggerFactory.getLogger

internal enum class RaftRole {

    FOLLOWER {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.resetElectionTimeout(now)
        }

        override fun exit(now: Long, raft: RaftEngine) {
            super.exit(now, raft)
            raft.leader = null
        }

        override fun append(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? {
            raft.resetElectionTimeout(now)
            raft.leaderCommitIndex = msg.leaderCommitIndex
            val matchIndex = raft.append(msg)

            raft.logConsistent = matchIndex > BEFORE_LOG
            if (raft.logConsistent) {
                raft.updateCommitIndex(matchIndex)
                raft.sendAck(msg.from, matchIndex)
            } else {
                val nackIndex = raft.nackIndex(msg)
                raft.sendNack(msg.from, nackIndex)
            }
            raft.leader = msg.from
            return null
        }

        override fun requestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? {
            val candidate = msg.from

            val grantVote = (raft.votedFor == null || raft.votedFor == candidate) &&
                (raft.lastLogTerm < msg.lastLogTerm ||
                    (raft.lastLogTerm == msg.lastLogTerm && raft.lastLogIndex <= msg.lastLogIndex))

            if (grantVote) {
                raft.votedFor = candidate
                raft.resetElectionTimeout(now)
            }
            raft.sendVote(candidate, grantVote)
            return null
        }
    },

    CANDIDATE {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.apply {
                term++
                votedFor = self
                votesReceived.clear()
                votesReceived.add(raft.self)
                resetElectionTimeout(now)
                sendRequestVotes()
            }
        }

        override fun vote(now: Long, msg: VoteMessage, raft: RaftEngine): RaftRole? {
            log.info("vote from={} term={}", msg.from, msg.term)
            if (msg.vote) {
                raft.votesReceived.add(msg.from)
                val votesReceived = raft.votesReceived.size
                if (votesReceived >= raft.cluster.majority) {
                    return LEADER
                }
            }
            return null
        }

        override fun append(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? {
            return FOLLOWER
        }
    },

    LEADER {
        override fun run(now: Long, raft: RaftEngine) {
            super.run(now, raft)
            raft.followers.run(now)
        }

        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.cancelElectionTimeout()
            raft.followers.reset()
            raft.leader = raft.self

            val flush = flush(raft.term)
            raft.log.append(flush)
        }

        override fun exit(now: Long, raft: RaftEngine) {
            super.exit(now, raft)
            raft.leader = null
        }

        override fun clientAppend(now: Long, msg: ClientAppendMessage, raft: RaftEngine): Long? {
            val lastLogIndex = raft.log.append(msg.data)
            if (raft.singleNodeCluster) {
                raft.leaderCommitIndex = lastLogIndex
                raft.updateCommitIndex(lastLogIndex)
            }
            return lastLogIndex
        }

        override fun appendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? {
            return null
        }
    },

    ERROR {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.resetElectionTimeout(FOREVER)
        }
    };

    protected val log = getLogger("${RaftRole::class.java.name}.$name")!!

    protected open fun run(now: Long, raft: RaftEngine) {}

    protected open fun enter(now: Long, raft: RaftEngine) {}

    protected open fun exit(now: Long, raft: RaftEngine) {
        raft.votedFor = null
        raft.votesReceived.clear()
    }

    protected open fun clientAppend(now: Long, msg: ClientAppendMessage, raft: RaftEngine): Long? = null

    protected open fun append(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? = null

    protected open fun appendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? = null

    protected open fun requestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? = null

    protected open fun vote(now: Long, msg: VoteMessage, raft: RaftEngine): RaftRole? = null

    private fun processMessage(now: Long, msg: RaftMessage, raft: RaftEngine) = when (msg.type) {
        APPEND -> append(now, msg as AppendMessage, raft)
        APPEND_ACK -> appendAck(now, msg as AppendAckMessage, raft)
        REQUEST_VOTE -> requestVote(now, msg as RequestVoteMessage, raft)
        VOTE -> vote(now, msg as VoteMessage, raft)
    }

    fun process(now: Long, msg: RaftMessage, raft: RaftEngine): RaftRole? {
        val term = msg.term

        if (raft.term < term) {
            raft.term = term
            return if (this == ERROR) ERROR else FOLLOWER
        }

        return if (raft.term == term) {
            processMessage(now, msg, raft)
        } else {
            if (msg.type == REQUEST_VOTE) raft.sendVote(msg.from, false)
            null
        }
    }
}
