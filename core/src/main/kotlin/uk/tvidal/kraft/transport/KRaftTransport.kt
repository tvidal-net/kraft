package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import java.io.Closeable

interface KRaftTransport : Closeable {

    val self: RaftNode

    fun sender(node: RaftNode): MessageSender

    fun receiver(): MessageReceiver
}
