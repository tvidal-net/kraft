package uk.tvidal.kraft.transport

import uk.tvidal.kraft.domain.RaftNode

interface KRaftTransport {

    fun sender(node: RaftNode): MessageSender

    fun register(node: RaftNode, receiver: MessageReceiver)
}
