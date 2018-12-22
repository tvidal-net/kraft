package uk.tvidal.kraft.engine

import uk.tvidal.kraft.NEVER
import uk.tvidal.kraft.NOW
import uk.tvidal.kraft.domain.RaftNode
import uk.tvidal.kraft.logging.KRaftLogger
import uk.tvidal.kraft.message.raft.AppendAckMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import uk.tvidal.kraft.storage.KRaftEntries
import uk.tvidal.kraft.storage.emptyEntries
import uk.tvidal.kraft.transport.MessageSender
import java.util.concurrent.atomic.AtomicInteger

internal class RaftFollowerState(
    val raft: RaftEngine,
    private val sender: MessageSender
) {

    private val log = KRaftLogger("${RaftFollowerState::class.java.name}.${sender.node}")

    val follower: RaftNode = sender.node

    var nextHeartbeat = NEVER
        private set

    var streaming = false
        private set

    var nextIndex = 0L
        private set

    var matchIndex = 0L
        private set

    private val byteLimit = AtomicInteger()

    init {
        resetByteWindow()
    }

    fun reset() {
    }

    fun run(now: Long) {
        if (streaming) {
            val data = raft.storage.read(nextIndex, byteLimit.get())
            if (data.isEmpty) return // not enough bytes to send the next entry

            if (sendHeartbeat(data)) {
                updateHeartbeat(now)
            }
        } else heartbeat(now)
    }

    private fun heartbeat(now: Long) {
        if (now >= nextHeartbeat && sendHeartbeat()) {
            updateHeartbeat(now)
        }
    }

    fun commit() {
        if (streaming) nextHeartbeat = NOW
    }

    fun ack(msg: AppendAckMessage) {
        TODO("Process $msg")
    }

    private fun sendHeartbeat(data: KRaftEntries = emptyEntries()) = when {
        data.isEmpty || consumeByteWindow(data.bytes) -> {
            val prevIndex = nextIndex - 1
            val prevTerm = raft.storage.termAt(prevIndex)

            nextIndex += data.size
            send(raft.heartbeat(prevIndex, prevTerm, data))
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
            val prevLimit = byteLimit.get()
            newLimit = prevLimit - bytes
        } while (newLimit >= 0 && !byteLimit.compareAndSet(prevLimit, newLimit))
        return newLimit >= 0
    }

    private fun resetByteWindow() {
        byteLimit.set(raft.sizes.maxUnackedBytesWindow)
    }

    private fun updateHeartbeat(now: Long) {
        nextHeartbeat = now + raft.heartbeatWindowMillis
    }
}
