package uk.tvidal.kraft.transport

import uk.tvidal.kraft.message.Message

interface MessageReceiver {

    val size: Int

    fun poll(): Message?

    fun offer(message: Message): Boolean

    fun removeIf(predicate: (Message) -> Boolean)
}
