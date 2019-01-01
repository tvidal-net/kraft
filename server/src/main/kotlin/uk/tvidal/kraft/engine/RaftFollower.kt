package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.emptyEntries
import uk.tvidal.kraft.streaming.DataWindow
import uk.tvidal.kraft.transport.MessageSender
import kotlin.math.min

class RaftFollower internal constructor(
    private val raft: RaftEngine,
    private val sender: MessageSender
) {
    private companion object : KRaftLogging()

    val follower: RaftNode
        get() = sender.node

    internal var nextHeartbeatTime = NEVER
        private set

    val streaming: Boolean
        get() = matchIndex >= BEFORE_LOG

    var nextIndex = BEFORE_LOG
        private set

    var matchIndex = BEFORE_LOG
        private set

    private val window = DataWindow(raft.sizes.unackedBytes)

    init {
        reset()
    }

    internal fun reset() {
        window.reset()
        matchIndex = BEFORE_LOG
        nextIndex = raft.nextLogIndex
    }

    internal fun resetHeartbeatTimeout(now: Long) {
        nextHeartbeatTime = now + raft.heartbeatWindow
    }

    internal fun clearHeartbeatTimeout() {
        nextHeartbeatTime = NOW
    }

    fun work(now: Long) {
        if (streaming) {
            val available = min(raft.sizes.batchSize, window.available)
            val data = raft.read(nextIndex, available)
            if (!data.isEmpty) { // not enough bytes to send the next entry
                window.consume(nextIndex, data)
                appendEntries(now, data)
                nextIndex += data.size
                return
            }
        }
        heartbeat(now)
    }

    private fun heartbeat(now: Long) {
        if (now >= nextHeartbeatTime) {
            appendEntries(now)
        }
    }

    internal fun ack(newMatchIndex: Long) {
        window.release(newMatchIndex)
        matchIndex = newMatchIndex
        raft.computeCommitIndex()
    }

    internal fun nack(nackIndex: Long) {
        matchIndex = NEVER
        nextIndex = nackIndex + 1
        window.reset()
        clearHeartbeatTimeout()
    }

    internal fun commit() {
        if (streaming) {
            clearHeartbeatTimeout()
        }
    }

    private fun appendEntries(now: Long, data: KRaftEntries = emptyEntries()) {
        resetHeartbeatTimeout(now)
        val prevIndex = nextIndex - 1
        val prevTerm = raft.termAt(prevIndex)
        raft.append(follower, prevIndex, prevTerm, data)
    }
}
