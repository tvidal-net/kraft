package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.KRaftError
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.clientNode
import uk.tvidal.kraft.config.KRaftServerConfig
import uk.tvidal.kraft.engine.RaftRole.CANDIDATE
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.monitor.GarbageCollectorListener
import uk.tvidal.kraft.storage.flush
import java.lang.System.currentTimeMillis
import java.time.Instant

abstract class RaftEngine internal constructor(
    config: KRaftServerConfig
) : RaftState, RaftMessageSender, AutoCloseable {

    private companion object : KRaftLogging() {
        init {
            GarbageCollectorListener.install()
        }
    }

    val clientNode: RaftNode = clientNode()

    final override val cluster = config.cluster

    override val transport = config.transport
    protected val storage = config.storage
    private val timeout = config.timeout
    internal val sizes = config.sizes

    final override val lastLogTerm
        get() = storage.lastLogTerm

    final override val lastLogIndex
        get() = storage.lastLogIndex

    final override val nextLogIndex
        get() = storage.nextLogIndex

    final override var role = if (cluster.single) LEADER else FOLLOWER
        protected set

    final override var term = lastLogTerm
        protected set

    final override var leaderCommitIndex = 0L
        protected set

    final override var commitIndex = 0L
        protected set(value) {
            if (leaderCommitIndex > field) {
                field = minOf(leaderCommitIndex, value)
            }
        }

    final override var logConsistent = false
        private set

    final override var leader: RaftNode? = null
        private set

    final override var votedFor: RaftNode? = null
        private set

    override val votesReceived: MutableSet<RaftNode> = mutableSetOf()

    var nextElectionTime = timeout.firstElectionTime(currentTimeMillis())
        private set

    private var lastElectionTimeChecked: Long = Long.MAX_VALUE

    init {
        log.info { "Starting: $this (election=${Instant.ofEpochMilli(nextElectionTime)})" }
    }

    internal val heartbeatWindow: Int
        get() = timeout.heartbeatTimeout

    internal fun startElection(now: Long) {
        log.trace { "[$self] startElection T$term" }
        updateTerm()
        votedFor = self
        votesReceived.clear()
        votesReceived += self
        resetElectionTimeout(now)
        requestVotes()
    }

    internal fun resetElection() {
        votedFor = null
        votesReceived.clear()
    }

    internal fun resetElectionTimeout(now: Long) {
        nextElectionTime = timeout.nextElectionTime(now)
    }

    internal fun cancelElectionTimeout() {
        nextElectionTime = NEVER
    }

    internal fun checkElectionTimeout(now: Long): RaftRole? = try {
        // nextElectionTime > 0 && nextElectionTime <= now
        if (nextElectionTime in 1..now) {
            val delay = now - lastElectionTimeChecked
            if (delay > heartbeatWindow) {
                resetElectionTimeout(now)
                log.warn { "[$self] checkElectionTimeout check took too long ($delay ms), resetting" }
                null
            } else {
                CANDIDATE
            }
        } else null
    } finally {
        lastElectionTimeChecked = now
    }

    internal fun resetLeader() {
        leader = null
    }

    internal fun appendEntries(now: Long, msg: AppendMessage) {
        resetElectionTimeout(now)
        leader = msg.from
        leaderCommitIndex = msg.leaderCommitIndex
        val matchIndex = appendLogIndex(msg.prevIndex, msg.prevTerm)
        logConsistent = matchIndex > BEFORE_LOG
        if (logConsistent) {
            storage.append(msg.data, matchIndex)
            commitIndex = msg.prevIndex
            ack(msg.from, lastLogIndex)
        } else {
            val nackIndex = nackIndex(msg)
            nack(msg.from, nackIndex)
        }
    }

    private fun appendLogIndex(prevIndex: Long, prevTerm: Long): Long {
        val termAtPrevIndex = storage.termAt(prevIndex)
        val logMessage = "[$self] appendLogIndex ($leader) prevIndex=$prevIndex " +
            "termAtPrevIndex=[$termAtPrevIndex,msg=$prevTerm]"

        if (prevIndex == 0L || (prevIndex <= lastLogIndex && prevTerm == termAtPrevIndex)) {

            if (prevIndex < lastLogIndex) {
                if (commitIndex > prevIndex) {
                    log.error { "$logMessage - CANNOT TRUNCATE BEFORE COMMIT_INDEX: $commitIndex" }
                    return BEFORE_LOG
                } else {
                    log.info { "$logMessage - TRUNCATE LOG" }
                }
            } else {
                log.debug { "$logMessage - OK" }
            }
            return prevIndex + 1
        }
        log.warn { "$logMessage - LOG IS INCONSISTENT" }
        return BEFORE_LOG
    }

    private fun nackIndex(msg: AppendMessage): Long = when {
        msg.prevIndex > lastLogIndex -> lastLogIndex
        msg.prevIndex > 0 -> msg.prevIndex - 1
        else -> throw KRaftError("received prevIndex=${msg.prevIndex} from ${msg.from}")
    }

    internal fun processRequestVote(now: Long, msg: RequestVoteMessage) {
        val candidate = msg.from
        val grantVote = (votedFor == null || votedFor == candidate) &&
            (lastLogTerm < msg.lastLogTerm ||
                (lastLogTerm == msg.lastLogTerm && lastLogIndex <= msg.lastLogIndex))

        if (grantVote) {
            votedFor = candidate
            resetElectionTimeout(now)
        }
        vote(candidate, grantVote)
    }

    internal fun processVote(message: VoteMessage): RaftRole? {
        log.info { "[$self] processVote T$term $message" }
        if (message.vote) {
            votesReceived += message.from
            if (votesReceived.size >= cluster.majority) {
                return LEADER
            }
        }
        return null
    }

    internal fun becomeLeader() {
        cancelElectionTimeout()
        resetFollowers()
        leader = self
        appendFlush()
    }

    internal abstract fun processAck(msg: AppendAckMessage)

    internal abstract fun updateTerm(newTerm: Long = term + 1)

    internal fun termAt(index: Long) = storage.termAt(index)

    internal fun read(fromIndex: Long, byteLimit: Int) = storage.read(fromIndex, byteLimit)

    private fun appendFlush() = storage.append(entries = flush(term))

    internal abstract fun heartbeatFollowers(now: Long)

    internal abstract fun resetFollowers()

    internal abstract fun computeCommitIndex()

    internal abstract fun run(now: Long)

    abstract fun execute(block: () -> Unit)

    abstract fun publish(payload: ByteArray)

    override fun toString() = "${RaftEngine::class.simpleName}($self) $storage"
}
