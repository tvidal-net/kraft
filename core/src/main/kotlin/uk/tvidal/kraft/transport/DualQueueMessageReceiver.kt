package uk.tvidal.kraft.transport

import uk.tvidal.kraft.logging.KRaftLogging
import uk.tvidal.kraft.message.Message
import uk.tvidal.kraft.message.client.ClientMessage
import uk.tvidal.kraft.message.raft.RaftMessage
import java.lang.Thread.currentThread
import java.util.ArrayDeque
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue

class DualQueueMessageReceiver(
    private val raft: BlockingQueue<Message>,
    private val client: BlockingQueue<Message>,
    private val maxDrainCount: Int = MAX_DRAIN_COUNT
) : MessageReceiver {

    constructor(
        raftSize: Int = 64,
        clientSize: Int = 4096,
        maxDrainCount: Int = MAX_DRAIN_COUNT
    ) : this(
        raft = createIncomingMessageQueue(raftSize),
        client = createIncomingMessageQueue(clientSize),
        maxDrainCount = maxDrainCount
    )

    private val messages = ArrayDeque<Message>(maxDrainCount)

    internal companion object : KRaftLogging() {
        private const val MAX_DRAIN_COUNT = 16

        private fun createIncomingMessageQueue(queueSize: Int): BlockingQueue<Message> = when {
            queueSize <= 0 -> SynchronousQueue(true)
            else -> ArrayBlockingQueue(queueSize, true)
        }
    }

    override val size: Int
        get() = raft.size + client.size + messages.size

    override fun poll(): Message? {
        if (messages.isEmpty()) {
            raft.drainTo(messages, maxDrainCount)
            client.drainTo(messages, maxDrainCount - messages.size)
        }
        return messages.poll()
    }

    override fun offer(message: Message): Boolean = try {
        when (message) {
            is RaftMessage -> raft.put(message)
            is ClientMessage -> client.put(message)
            else -> log.warn { "received an unknown message type: $message" }
        }
        true
    } catch (e: InterruptedException) {
        currentThread().interrupt()
        false
    }

    override fun removeIf(predicate: (Message) -> Boolean) {
        client.removeIf(predicate)
        raft.removeIf(predicate)
        messages.removeIf(predicate)
    }
}
