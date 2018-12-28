package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractClientMessage(
    override val type: ClientMessageType
) : AbstractMessage(), ClientMessage
