package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode

interface KRaftTransport {

    fun sender(node: RaftNode): MessageSender

    fun receive(node: RaftNode): MessageReceiver

}
