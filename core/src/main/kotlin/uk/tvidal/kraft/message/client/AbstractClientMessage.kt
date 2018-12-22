package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.domain.RaftNode
import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractClientMessage(

    final override val type: ClientMessageType,

    from: RaftNode

) : AbstractMessage(type, from), ClientMessage
