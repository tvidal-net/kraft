package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue

class BlockingMessageReceiver(
    private val queue: BlockingQueue<Message> = SynchronousQueue()
) : MessageReceiver {

    override val size: Int
        get() = queue.size

    override fun poll(): Message? = queue.take()

    override fun offer(message: Message?) = queue.offer(message)

    override fun removeIf(predicate: (Message) -> Boolean) {
        queue.removeIf(predicate)
    }
}
