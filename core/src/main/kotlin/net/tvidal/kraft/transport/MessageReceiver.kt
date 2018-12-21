package net.tvidal.kraft.transport

import net.tvidal.kraft.message.Message

interface MessageReceiver {

    val size: Int

    fun poll(): Message

    fun offer(message: Message): Boolean
}
