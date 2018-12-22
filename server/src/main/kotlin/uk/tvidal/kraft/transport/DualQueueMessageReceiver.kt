package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.raft.RaftMessage
import java.lang.Thread.currentThread
import java.util.ArrayDeque
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue

class DualQueueMessageReceiver(
    private val raftQueue: BlockingQueue<Message>,
    private val clientQueue: BlockingQueue<Message>,
    private val maxDrainCount: Int = MAX_DRAIN_COUNT
) : MessageReceiver {

    constructor(
        raftQueueSize: Int = 64,
        clientQueueSize: Int = 4096,
        maxDrainCount: Int = MAX_DRAIN_COUNT
    ) : this(
        raftQueue = createIncomingMessageQueue(raftQueueSize),
        clientQueue = createIncomingMessageQueue(clientQueueSize),
        maxDrainCount = maxDrainCount
    )

    private val messages = ArrayDeque<Message>(maxDrainCount)

    companion object {
        private const val MAX_DRAIN_COUNT = 10
        private fun createIncomingMessageQueue(queueSize: Int): BlockingQueue<Message> = when {
            queueSize <= 0 -> SynchronousQueue(true)
            else -> ArrayBlockingQueue(queueSize, true)
        }
    }

    override val size: Int
        get() = raftQueue.size + clientQueue.size + messages.size

    override fun poll(): Message? {
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

    override fun removeIf(predicate: (Message) -> Boolean) {
        messages.removeIf(predicate)
        raftQueue.removeIf(predicate)
        clientQueue.removeIf(predicate)
    }
}
