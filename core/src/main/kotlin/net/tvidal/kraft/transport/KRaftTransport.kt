package net.tvidal.kraft.transport

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.Message
import java.util.function.Consumer

interface KRaftTransport {

    fun sender(node: RaftNode): MessageSender

    fun register(node: RaftNode, receiver: MessageReceiver)

}
