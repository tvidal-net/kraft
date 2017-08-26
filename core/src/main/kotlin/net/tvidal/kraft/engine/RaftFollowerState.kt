package net.tvidal.kraft.processing

import net.tvidal.kraft.NEVER
import net.tvidal.kraft.NOW
import net.tvidal.kraft.message.raft.AppendAckMessage
import net.tvidal.kraft.message.raft.RaftMessage
import net.tvidal.kraft.storage.KRaftEntries
import net.tvidal.kraft.storage.emptyEntries
import net.tvidal.kraft.transport.MessageSender
import org.slf4j.LoggerFactory.getLogger
import java.util.concurrent.atomic.AtomicInteger

internal class RaftFollowerState(

  val raft: RaftEngine,
  private val sender: MessageSender

) {

    private val LOG = getLogger("${RaftFollowerState::class.java.name}.${sender.node}")

    val follower = sender.node

    var nextHeartbeat = NEVER; private set
    var streaming = false; private set

    var nextIndex = 0L; private set

    var matchIndex = 0L; private set

    private val byteLimit = AtomicInteger()

    init {
        resetByteWindow()
    }

    fun reset() {

    }

    fun run(now: Long) {
        if (streaming) {
            val data = raft.log.read(nextIndex, byteLimit.get())
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
    }

    private fun sendHeartbeat(data: KRaftEntries = emptyEntries()) = when {
        data.isEmpty || consumeByteWindow(data.bytes) -> {
            val prevIndex = nextIndex - 1
            val prevTerm = raft.log.termAt(prevIndex)

            nextIndex += data.size
            send(raft.heartbeat(prevIndex, prevTerm, data))
        }
        else -> false
    }

    private fun send(msg: RaftMessage) = try {
        sender.send(msg)
        true
    } catch (e: Exception) {
        LOG.error("Error sending message [{}]", msg, e)
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
