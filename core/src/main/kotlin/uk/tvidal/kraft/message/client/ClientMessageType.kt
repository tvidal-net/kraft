package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.message.MessageType
import kotlin.reflect.KClass

enum class ClientMessageType(
    override val messageType: KClass<out ClientMessage>? = null
) : MessageType {

    CLIENT_APPEND(ClientAppendMessage::class),
    CLIENT_APPEND_ACK,

    CONSUMER_REGISTER(ConsumerRegisterMessage::class),
    CONSUMER_DATA(ConsumerDataMessage::class),
    CONSUMER_ACK(ConsumerAckMessage::class),

    CLIENT_ERROR;
}
