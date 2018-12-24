package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class SimpleMessageReceiver(
    private val queue: BlockingQueue<Message>
) : MessageReceiver {

    constructor(capacity: Int = 64) : this(ArrayBlockingQueue(capacity))

    override val size: Int
        get() = queue.size

    override fun poll(): Message? = queue.poll()

    override fun offer(message: Message?) = queue.offer(message)

    override fun removeIf(predicate: (Message) -> Boolean) {
        queue.removeIf(predicate)
    }
}
