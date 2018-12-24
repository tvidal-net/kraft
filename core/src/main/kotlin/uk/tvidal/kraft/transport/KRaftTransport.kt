package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode

interface KRaftTransport {

    val self: RaftNode

    fun sender(node: RaftNode): MessageSender

    fun receiver(): MessageReceiver
}
