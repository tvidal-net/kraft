package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractTransportMessage(
    override val type: TransportMessageType
) : AbstractMessage(), TransportMessage
