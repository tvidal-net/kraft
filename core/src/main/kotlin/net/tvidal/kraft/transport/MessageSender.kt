package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message

interface MessageSender {

    val node: RaftNode

    fun send(message: Message)

    fun respond(message: Message)

}
