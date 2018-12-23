package uk.tvidal.kraft.engine

import uk.tvidal.kraft.BEFORE_LOG
import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.emptyEntries
import uk.tvidal.kraft.transport.MessageSender
import java.util.concurrent.atomic.AtomicInteger

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

    private val byteLimit = AtomicInteger()

    val availableBytes: Int
        get() = byteLimit.get()

    init {
        reset()
    }

    internal fun reset() {
        resetByteWindow()
        matchIndex = BEFORE_LOG
        nextIndex = raft.nextLogIndex
    }

    internal fun resetHeartbeatTimeout(now: Long) {
        nextHeartbeatTime = now + raft.heartbeatWindow
    }

    internal fun clearHeartbeatTimeout() {
        nextHeartbeatTime = NOW
    }

    internal fun run(now: Long) {
        if (streaming) {
            val data = raft.read(nextIndex, availableBytes)
            if (!data.isEmpty) { // not enough bytes to send the next entry
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
        matchIndex = newMatchIndex
        raft.computeCommitIndex()
    }

    internal fun nack(nackIndex: Long) {
        matchIndex = NEVER
        nextIndex = nackIndex + 1
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

    private fun consumeByteWindow(bytes: Int): Boolean {
        var newLimit: Int
        do {
            val prevLimit = availableBytes
            newLimit = prevLimit - bytes
        } while (newLimit >= 0 && !byteLimit.compareAndSet(prevLimit, newLimit))
        return newLimit >= 0
    }

    private fun resetByteWindow() {
        byteLimit.set(raft.sizes.maxUnackedBytesWindow)
    }
}
