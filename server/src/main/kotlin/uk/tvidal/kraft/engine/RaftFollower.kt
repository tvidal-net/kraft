package uk.tvidal.kraft.engine

import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.emptyEntries
import uk.tvidal.kraft.transport.MessageSender
import java.util.concurrent.atomic.AtomicInteger

internal class RaftFollower(
    val raft: RaftEngine,
    private val sender: MessageSender
) {

    private companion object : KRaftLogging()

    val follower: RaftNode
        get() = sender.node

    var nextHeartbeatTime = NEVER
        private set

    var streaming = false
        private set

    var nextIndex = 0L
        private set

    var matchIndex = 0L
        private set

    private val byteLimit = AtomicInteger()

    init {
        reset()
    }

    fun reset() {
        resetByteWindow()
        streaming = false
        nextIndex = raft.lastLogIndex + 1
        matchIndex = 0L
    }

    fun run(now: Long) {
        if (streaming) {
            val data = raft.read(nextIndex, byteLimit.get())
            if (data.isEmpty) return // not enough bytes to send the next entry

            if (sendHeartbeat(data)) {
                updateHeartbeat(now)
            }
        } else heartbeat(now)
    }

    private fun heartbeat(now: Long) {
        if (now >= nextHeartbeatTime && sendHeartbeat()) {
            updateHeartbeat(now)
        }
    }

    fun commit() {
        if (streaming) nextHeartbeatTime = NOW
    }

    fun ack(matchIndex: Long) {
        TODO("Process $matchIndex")
    }

    fun nack(nackIndex: Long) {
        TODO("Process $nackIndex")
    }

    private fun sendHeartbeat(data: KRaftEntries = emptyEntries()) = when {
        data.isEmpty || consumeByteWindow(data.bytes) -> {
            val prevIndex = nextIndex - 1
            val prevTerm = raft.termAt(prevIndex)

            nextIndex += data.size
            raft.heartbeat(follower, prevIndex, prevTerm)
            raft.append(follower, prevIndex, prevTerm, data)
            true
        }
        else -> false
    }

    private fun send(msg: RaftMessage) = try {
        sender.send(msg)
        true
    } catch (e: Exception) {
        log.error("Error sending message [{}]", msg, e)
        false
    }

    private fun consumeByteWindow(bytes: Int): Boolean {
        var newLimit: Int
        do {
            val prevLimit = byteLimit.toInt()
            newLimit = prevLimit - bytes
        } while (newLimit >= 0 && !byteLimit.compareAndSet(prevLimit, newLimit))
        return newLimit >= 0
    }

    private fun resetByteWindow() {
        byteLimit.set(raft.sizes.maxUnackedBytesWindow)
    }

    private fun updateHeartbeat(now: Long) {
        nextHeartbeatTime = now + raft.heartbeatWindow
    }
}
