package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.raft.RaftMessage
import java.lang.Thread.currentThread
import java.util.ArrayDeque
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue

class DualQueueMessageReceiver(raftQueueSize: Int = 0, clientQueueSize: Int = 0, private val maxDrainCount: Int = 10) : MessageReceiver {

    private val raftQueue = createIncomingMessageQueue(raftQueueSize)
    private val clientQueue = createIncomingMessageQueue(clientQueueSize)
    private val messages = ArrayDeque<Message>(maxDrainCount)

    companion object {
        private fun createIncomingMessageQueue(queueSize: Int): BlockingQueue<Message> = when {
            queueSize <= 0 -> SynchronousQueue(true)
            else -> ArrayBlockingQueue(queueSize, true)
        }
    }

    override val size: Int
        get() = raftQueue.size + clientQueue.size

    override fun poll(): Message {
        if (messages.isEmpty()) {
            raftQueue.drainTo(messages, maxDrainCount - messages.size)
            clientQueue.drainTo(messages, maxDrainCount - messages.size)
        }
        return messages.poll()
    }

    override fun offer(message: Message): Boolean = try {
        val queue = if (message is RaftMessage) raftQueue else clientQueue
        queue.put(message)
        true
    } catch (e: InterruptedException) {
        currentThread().interrupt()
        false
    }
}
