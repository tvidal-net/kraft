package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode

interface KRaftTransport {

    fun sender(node: RaftNode): MessageSender

    fun register(node: RaftNode, receiver: MessageReceiver)
}
