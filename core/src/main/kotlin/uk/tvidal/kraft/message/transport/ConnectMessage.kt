package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.transport.TransportMessageType.CONNECT

class ConnectMessage(
    from: RaftNode
) : AbstractTransportMessage(CONNECT, from)
