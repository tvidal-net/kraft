package net.tvidal.kraft.transport

import net.tvidal.kraft.message.Message

interface MessageSender {

    fun send(message: Message)

    fun respond(message: Message)

}
