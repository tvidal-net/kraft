package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.raft.RaftMessage

interface MessageSender {

    fun send(to: RaftNode, message: RaftMessage)

}
