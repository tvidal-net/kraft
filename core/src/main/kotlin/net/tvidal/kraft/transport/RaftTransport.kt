package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message

interface RaftTransport {

    fun sendMessage(to: RaftNode, message: Message)

    fun poll(): Message?

}
