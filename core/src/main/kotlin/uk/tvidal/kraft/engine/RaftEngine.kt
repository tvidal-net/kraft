package uk.tvidal.kraft.engine

import uk.tvidal.kraft.KRaftError
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.domain.RaftNode
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.message.raft.AppendMessage

internal abstract class RaftEngine(
    config: KRaftConfig
) : RaftState, RaftMessageSender {

    override val cluster = config.cluster

    override val transport = config.transport
    protected val storage = config.storage
    protected val timeout = config.timeout
    protected val sizes = config.size

    override var role = FOLLOWER
    override var term = 0L

    final override var commitIndex = 0L
        private set

    final override var leaderCommitIndex = 0L
        internal set

    final override var lastLogTerm = 0L
        private set

    final override var lastLogIndex = 0L
        private set

    override var logConsistent = false

    override var leader: RaftNode? = null
    override var votedFor: RaftNode? = null
    override val votesReceived: MutableSet<RaftNode> = mutableSetOf()

    private var nextElectionTime = NEVER

    fun resetElection() {
        votedFor = null
        votesReceived.clear()
    }

    fun resetElectionTimeout(now: Long) {
        nextElectionTime = timeout.nextElectionTime(now)
    }

    fun cancelElectionTimeout() {
        nextElectionTime = NEVER
    }

    fun updateCommitIndex(matchIndex: Long) {
        if (leaderCommitIndex > commitIndex) {
            commitIndex = minOf(leaderCommitIndex, matchIndex)
        }
    }

    fun read(fromIndex: Long, byteLimit: Int) = storage.read(fromIndex, byteLimit)

    fun termAt(index: Long) = storage.termAt(index)

    abstract fun append(msg: AppendMessage): Long

    fun nackIndex(msg: AppendMessage): Long {
        val leaderPrevIndex = msg.prevIndex
        return when {
            leaderPrevIndex > lastLogIndex -> lastLogIndex
            leaderPrevIndex > 0 -> leaderPrevIndex - 1
            else -> throw KRaftError("received prevIndex=$leaderPrevIndex from ${msg.from}")
        }
    }

    abstract fun flush(): Long
}
