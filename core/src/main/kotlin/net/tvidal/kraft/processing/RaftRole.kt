package net.tvidal.kraft.processing

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
import net.tvidal.kraft.message.raft.ack
import net.tvidal.kraft.message.raft.nack
import net.tvidal.kraft.message.raft.requestVotes
import net.tvidal.kraft.message.raft.vote
import net.tvidal.kraft.storage.flush
import org.slf4j.LoggerFactory.getLogger

enum class RaftRole {

    FOLLOWER {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.resetElectionTimeout(now)
        }

        override fun exit(now: Long, raft: RaftEngine) {
            super.exit(now, raft)
            raft.leader = null
        }

        override fun handleAppend(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? {
            raft.resetElectionTimeout(now)
            raft.leaderCommitIndex = msg.leaderCommitIndex
            val matchIndex = raft.append(msg)

            raft.logConsistent = matchIndex > 0L
            if (raft.logConsistent) {
                raft.updateCommitIndex(matchIndex)
                raft.ack(msg.from, matchIndex)
            } else {
                val nackIndex = raft.nackIndex(msg)
                raft.nack(msg.from, nackIndex)
            }
            raft.leader = msg.from
            return null
        }

        override fun handleRequestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? {
            val candidate = msg.from

            val grantVote = (raft.votedFor == null || raft.votedFor == candidate) &&
              (raft.lastLogTerm < msg.lastLogTerm ||
                (raft.lastLogTerm == msg.lastLogTerm && raft.lastLogIndex <= msg.lastLogIndex))

            if (grantVote) {
                raft.votedFor = candidate
                raft.resetElectionTimeout(now)
            }
            raft.vote(candidate, grantVote)
            return null
        }
    },

    CANDIDATE {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.term++
            raft.votedFor = raft.self
            raft.votesReceived.clear()
            raft.votesReceived.add(raft.self)
            raft.resetElectionTimeout(now)
            raft.requestVotes()
        }

        override fun handleVote(now: Long, msg: VoteMessage, raft: RaftEngine): RaftRole? {
            if (msg.vote) {
                raft.votesReceived.add(msg.from)
                if (raft.votesReceived.size >= raft.cluster.majority) {
                    return LEADER
                }
            }
            return null
        }

        override fun handleAppend(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? {
            return FOLLOWER
        }
    },

    LEADER {
        override fun work(now: Long, raft: RaftEngine) {
            super.work(now, raft)
            raft.followers.work(now)
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
            val lastLogIndex = raft.log.append(msg.entries)
            if (raft.singleNodeCluster) {
                raft.leaderCommitIndex = lastLogIndex
                raft.updateCommitIndex(lastLogIndex)
            }
            return lastLogIndex
        }

        override fun handleAppendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? {
            return null
        }
    },

    ERROR {
        override fun enter(now: Long, raft: RaftEngine) {
            super.enter(now, raft)
            raft.resetElectionTimeout(FOREVER)
        }
    };

    protected val LOG = getLogger("${RaftRole::class.java}.$name")

    open fun work(now: Long, raft: RaftEngine) {}

    open fun enter(now: Long, raft: RaftEngine) {}

    open fun exit(now: Long, raft: RaftEngine) {
        raft.votedFor = null
        raft.votesReceived.clear()
    }

    open fun clientAppend(now: Long, msg: ClientAppendMessage, raft: RaftEngine): Long? = null

    open fun handleAppend(now: Long, msg: AppendMessage, raft: RaftEngine): RaftRole? = null

    open fun handleAppendAck(now: Long, msg: AppendAckMessage, raft: RaftEngine): RaftRole? = null

    open fun handleRequestVote(now: Long, msg: RequestVoteMessage, raft: RaftEngine): RaftRole? = null

    open fun handleVote(now: Long, msg: VoteMessage, raft: RaftEngine): RaftRole? = null

    private fun acceptMessage(now: Long, msg: RaftMessage, raft: RaftEngine) = when (msg.type) {
        APPEND -> handleAppend(now, msg as AppendMessage, raft)
        APPEND_ACK -> handleAppendAck(now, msg as AppendAckMessage, raft)
        REQUEST_VOTE -> handleRequestVote(now, msg as RequestVoteMessage, raft)
        VOTE -> handleVote(now, msg as VoteMessage, raft)
    }

    fun handleMessage(now: Long, msg: RaftMessage, raft: RaftEngine): RaftRole? {
        val term = msg.term

        if (raft.term < term) {
            raft.term = term
            return if (this == ERROR) ERROR else FOLLOWER
        }

        return if (raft.term == term) {
            acceptMessage(now, msg, raft)

        } else {
            if (msg.type == REQUEST_VOTE) {
                raft.vote(msg.from, false)
            }
            null
        }
    }

}
