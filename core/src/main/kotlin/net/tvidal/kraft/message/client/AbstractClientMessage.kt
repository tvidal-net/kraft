package net.tvidal.kraft.message.client

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.AbstractMessage

abstract class AbstractClientMessage(
    override val type: ClientMessageType,
    from: RaftNode
) : AbstractMessage(type, from), ClientMessage
