package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.message.MessageType
import kotlin.reflect.KClass

enum class TransportMessageType(
    override val messageType: KClass<out TransportMessage>? = null
) : MessageType {

    CONNECT(ConnectMessage::class);
}
