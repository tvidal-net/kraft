package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.transport.TransportMessageType.CONNECT

data class ConnectMessage(
    override val from: RaftNode
) : AbstractTransportMessage(CONNECT)
