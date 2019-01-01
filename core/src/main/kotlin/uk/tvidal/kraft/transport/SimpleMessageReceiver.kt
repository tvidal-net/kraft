package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message
import java.util.LinkedList
import java.util.Queue

class SimpleMessageReceiver(val queue: Queue<Message> = LinkedList()) : MessageReceiver {

    override val size: Int
        get() = queue.size

    override fun poll(): Message? = queue.poll()

    override fun offer(message: Message): Boolean = queue.offer(message)

    override fun removeIf(predicate: (Message) -> Boolean) {
        queue.removeIf(predicate)
    }
}
