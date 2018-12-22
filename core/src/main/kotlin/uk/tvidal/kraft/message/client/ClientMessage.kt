package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.message.Message

interface ClientMessage : Message {

    override val type: ClientMessageType
}
