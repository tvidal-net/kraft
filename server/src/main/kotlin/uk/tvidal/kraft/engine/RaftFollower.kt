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

internal class RaftFollower(
    private val raft: RaftEngine,
    private val sender: MessageSender
) {
    private companion object : KRaftLogging()

    val follower: RaftNode
        get() = sender.node

    var nextHeartbeatTime = NEVER
        private set

    val streaming: Boolean
        get() = matchIndex >= BEFORE_LOG

    var nextIndex = BEFORE_LOG
        private set

    var matchIndex = BEFORE_LOG
        private set

    private val byteLimit = AtomicInteger()

    private val availableBytes: Int
        get() = byteLimit.get()

    init {
        reset()
    }

    fun reset() {
        resetByteWindow()
        matchIndex = BEFORE_LOG
        nextIndex = raft.nextLogIndex
    }

    fun resetHeartbeatTimeout(now: Long) {
        nextHeartbeatTime = now + raft.heartbeatWindow
    }

    fun clearHeartbeatTimeout() {
        nextHeartbeatTime = NOW
    }

    fun run(now: Long) {
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

    fun ack(newMatchIndex: Long) {
        matchIndex = newMatchIndex
        raft.computeCommitIndex()
    }

    fun nack(nackIndex: Long) {
        matchIndex = NEVER
        nextIndex = nackIndex + 1
        clearHeartbeatTimeout()
    }

    fun commit() {
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
