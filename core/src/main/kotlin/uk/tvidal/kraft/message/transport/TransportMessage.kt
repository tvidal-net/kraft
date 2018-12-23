package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.message.Message

interface TransportMessage : Message {

    override val type: TransportMessageType
}
