package net.tvidal.kraft.message.client

import net.tvidal.kraft.message.Message

interface ClientMessage : Message {

    override val type: ClientMessageType
}
