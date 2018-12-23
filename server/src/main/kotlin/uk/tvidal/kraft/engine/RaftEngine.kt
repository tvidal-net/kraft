package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.KRaftError
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.client.localClientNode
import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftRole.CANDIDATE
import uk.tvidal.kraft.engine.RaftRole.FOLLOWER
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RequestVoteMessage
import uk.tvidal.kraft.message.raft.VoteMessage
import uk.tvidal.kraft.storage.flush
import java.lang.System.currentTimeMillis
import java.time.Instant

internal abstract class RaftEngine(
    config: KRaftConfig
) : RaftState, RaftMessageSender {

    companion object : KRaftLogging()

    override val clientNode: RaftNode = localClientNode()

    override val cluster = config.cluster

    override val transport = config.transport
    protected val storage = config.storage
    protected val timeout = config.timeout
    val sizes = config.sizes

    final override var role = FOLLOWER
        protected set

    final override var term = 0L
        private set

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

    final override val lastLogTerm
        get() = storage.lastLogTerm

    final override val lastLogIndex
        get() = storage.lastLogIndex

    final override val nextLogIndex
        get() = storage.nextLogIndex

    final override var leader: RaftNode? = null
        private set

    final override var votedFor: RaftNode? = null
        private set

    override val votesReceived: MutableSet<RaftNode> = mutableSetOf()

    internal var nextElectionTime = timeout.firstElectionTime(currentTimeMillis())
        private set

    init {
        log.info { "Created $this (election=${Instant.ofEpochMilli(nextElectionTime)})" }
    }

    val heartbeatWindow: Int
        get() = timeout.heartbeatTimeout

    fun startElection(now: Long) {
        log.info { "$self startElection T$term" }
        updateTerm()
        votedFor = self
        votesReceived.clear()
        votesReceived += self
        resetElectionTimeout(now)
        requestVotes()
    }

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

    fun checkElectionTimeout(now: Long): RaftRole? =
        if (nextElectionTime in 1..now) CANDIDATE
        else null

    fun resetLeader() {
        leader = null
    }

    fun appendEntries(now: Long, msg: AppendMessage) {
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
        val logMessage = "$self appendLogIndex ($leader) log=$lastLogIndex prevIndex=$prevIndex " +
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

    fun processRequestVote(now: Long, msg: RequestVoteMessage) {
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

    fun processVote(msg: VoteMessage): RaftRole? {
        log.info { "$self processVote T$term (${msg.from}) [vote=${msg.vote} term=${msg.term}]" }
        if (msg.vote) {
            votesReceived += msg.from
            if (votesReceived.size >= cluster.majority) {
                return LEADER
            }
        }
        return null
    }

    fun becomeLeader() {
        cancelElectionTimeout()
        resetFollowers()
        leader = self
        appendFlush()
    }

    abstract fun processAck(msg: AppendAckMessage)

    fun updateTerm(newTerm: Long = term + 1) {
        log.info { "$self updateTerm T$term newTerm=$newTerm" }
        term = newTerm
    }

    fun termAt(index: Long) = storage.termAt(index)

    fun read(fromIndex: Long, byteLimit: Int) = storage.read(fromIndex, byteLimit)

    private fun appendFlush() = storage.append(flush(term))

    abstract fun heartbeatFollowers(now: Long)

    abstract fun resetFollowers()

    abstract fun computeCommitIndex()

    abstract fun run(now: Long)

    override fun toString() = "${RaftEngine::class.simpleName}($self) $storage"
}
