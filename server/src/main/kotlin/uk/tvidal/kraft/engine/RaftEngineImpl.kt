package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.config.KRaftConfig
import uk.tvidal.kraft.engine.RaftRole.LEADER
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.client.ClientAppendMessage
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.AppendMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.flush
import uk.tvidal.kraft.transport.DualQueueMessageReceiver
import uk.tvidal.kraft.transport.MessageReceiver

internal class RaftEngineImpl(
    config: KRaftConfig,
    private val messages: MessageReceiver = DualQueueMessageReceiver()
) : RaftEngine(config) {

    private companion object : KRaftLogging()

    val followers = others
        .associate { it to RaftFollowerState(this, sender(it)) }

    init {
        transport.register(self, messages)
    }

    override fun flush() = storage.append(flush(term))

    fun clientAppend(msg: ClientAppendMessage) {
        if (role == LEADER) {
            val lastLogIndex = storage.append(msg.data)
            if (isSingleNodeCluster) {
                leaderCommitIndex = lastLogIndex
                updateCommitIndex(lastLogIndex)
            }
        }
    }

    override fun append(msg: AppendMessage) = appendLogIndex(msg).let {
        when {
            it > BEFORE_LOG -> storage.append(msg.data, it)
            else -> NEVER
        }
    }

    private fun appendLogIndex(msg: AppendMessage): Long {
        val prevTerm = msg.prevTerm
        val prevIndex = msg.prevIndex
        val termAtPrevIndex = storage.termAt(prevIndex)

        val logMessage = "from={} log={} prevIndex={} termAtPrevIndex=[{},from={}] - {}"
        val logData = arrayOf(msg.from, lastLogIndex, prevIndex, termAtPrevIndex, prevTerm)

        if (prevIndex == 0L || (prevIndex <= lastLogIndex && prevTerm == termAtPrevIndex)) {

            if (prevIndex < lastLogIndex) {
                if (commitIndex > prevIndex) {
                    log.warn(logMessage, *logData, "CANNOT TRUNCATE BEFORE COMMIT_INDEX: $commitIndex")
                    return BEFORE_LOG
                } else {
                    log.info(logMessage, *logData, "TRUNCATE LOG")
                }
            } else {
                log.debug(logMessage, *logData, "OK")
            }
            return prevIndex + 1
        }
        log.warn(logMessage, *logData, "LOG IS INCONSISTENT")
        return BEFORE_LOG
    }

    override fun run(now: Long) {
        val msg = messages.poll()
        when (msg) {
            is RaftMessage -> processMessage(now, msg)
            is AppendMessage -> append(msg)
        }
        role.run(now, this)
    }

    private fun processMessage(now: Long, msg: RaftMessage) {
        do {
            val newRole = role.process(now, msg, this)
            if (newRole != null && newRole != role) {
                role.exit(now, this)
                role = newRole
                role.enter(now, this)
            }
        } while (newRole != null)
    }

    override fun updateFollowers(now: Long) {
        followers.values.forEach { it.run(now) }
    }

    override fun resetFollowers() {
        followers.values.forEach(RaftFollowerState::reset)
    }

    fun updateCommitIndex() {
        val matchIndex = followers.values.map(RaftFollowerState::matchIndex) + lastLogIndex
        val quorumCommitIndex = matchIndex.sorted().take(cluster.majority).last()

        if (quorumCommitIndex > commitIndex) {

            val quorumCommitTerm = storage.termAt(quorumCommitIndex)
            if (quorumCommitTerm == term) {
                log.info("updateCommitIndex={} from={}", quorumCommitIndex, commitIndex)
                updateCommitIndex(quorumCommitIndex)
                followers.values.forEach(RaftFollowerState::commit)
            } else {
                log.warn(
                    "SKIPPING updateCommitIndex={} quorumCommitTerm={} currentTerm={}",
                    quorumCommitIndex, quorumCommitTerm, term
                )
            }
        }
    }

    override fun receiveAck(msg: AppendAckMessage) {
        val state = followers[msg.from]
        if (msg.ack) state?.ack(msg.matchIndex)
        else state?.nack(msg.matchIndex)
    }
}
