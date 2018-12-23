package uk.tvidal.kraft.codec.json

import uk.tvidal.kraft.message.MessageType
import uk.tvidal.kraft.message.client.ClientMessageType
import uk.tvidal.kraft.message.raft.RaftMessageType

object MessageCodec {

    val messageTypes = register(RaftMessageType.values()) +
        register(ClientMessageType.values())

    private fun register(types: Array<out MessageType>): Map<String, MessageType> = types
        .asSequence()
        .filter { it.messageType != null }
        .associate { it.name to it }
}
