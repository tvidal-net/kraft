package net.tvidal.kraft.message.client

import net.tvidal.kraft.message.MessageType

enum class ClientMessageType : MessageType {

    CLIENT_APPEND,
    CLIENT_APPEND_ACK,

    CONSUME_REGISTER,
    CONSUME_DATA,

    CLIENT_ERROR;
}
