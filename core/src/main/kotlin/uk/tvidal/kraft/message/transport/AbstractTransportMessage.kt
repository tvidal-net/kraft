package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.AbstractMessage

abstract class AbstractTransportMessage(

    final override val type: TransportMessageType,
    from: RaftNode

) : AbstractMessage(type, from), TransportMessage
