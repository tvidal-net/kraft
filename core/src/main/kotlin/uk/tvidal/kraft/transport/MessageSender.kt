package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message
import java.io.Closeable

interface MessageSender : Closeable {

    val self: RaftNode

    val node: RaftNode

    fun send(message: Message)

    fun respond(message: Message)
}
