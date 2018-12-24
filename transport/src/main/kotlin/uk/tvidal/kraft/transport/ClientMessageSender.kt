package uk.tvidal.kraft.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.Message

class ClientMessageSender(
    override val node: RaftNode,
    val client: ClientTransport
) : MessageSender {

    override fun send(message: Message) {
        client.write(message)
    }

    override fun respond(message: Message) {
        client.write(message)
    }
}
