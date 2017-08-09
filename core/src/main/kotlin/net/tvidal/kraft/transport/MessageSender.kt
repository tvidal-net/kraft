package net.tvidal.kraft.transport

import net.tvidal.kraft.message.raft.RaftMessage

interface MessageSender {

    fun send(message: RaftMessage)

}
