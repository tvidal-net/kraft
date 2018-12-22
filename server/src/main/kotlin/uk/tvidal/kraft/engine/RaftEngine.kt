package uk.tvidal.kraft.engine

import uk.tvidal.kraft.KRaftError
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import java.lang.System.currentTimeMillis
import java.time.Instant

internal abstract class RaftEngine(
    config: KRaftConfig
) : RaftState, RaftMessageSender {

    private companion object : KRaftLogging()

    override val cluster = config.cluster

    override val transport = config.transport
    protected val storage = config.storage
    protected val timeout = config.timeout
    val sizes = config.size

    override var role = FOLLOWER
    override var term = 0L

    final override var commitIndex = 0L
        internal set(value) {
            if (leaderCommitIndex > field) {
                field = minOf(leaderCommitIndex, value)
            }
        }

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

    internal var nextElectionTime = timeout.firstElectionTime(currentTimeMillis())
        private set

    init {
        log.info { "Starting $self on [$lastLogIndex:$lastLogTerm] (election=${Instant.ofEpochMilli(nextElectionTime)})" }
    }

    val heartbeatWindow: Int
        get() = timeout.heartbeatTimeout

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

    abstract fun receiveAck(msg: AppendAckMessage)

    abstract fun flush(): Long

    abstract fun updateFollowers(now: Long)

    abstract fun resetFollowers()

    abstract fun run(now: Long)

    override fun toString() = "${RaftEngine::class.simpleName}($self)"
}
