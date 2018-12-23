package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.message.MessageType
import kotlin.reflect.KClass

enum class ClientMessageType(
    override val messageType: KClass<out ClientMessage>? = null
) : MessageType {

    CLIENT_APPEND(ClientAppendMessage::class),
    CLIENT_APPEND_ACK,

    CONSUME_REGISTER,
    CONSUME_DATA,

    CLIENT_ERROR;
}
