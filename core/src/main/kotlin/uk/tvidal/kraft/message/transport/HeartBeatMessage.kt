package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.transport.TransportMessageType.HEARTBEAT

data class HeartBeatMessage(
    override val from: RaftNode,
    val time: Long,
    val ping: Boolean
) : AbstractTransportMessage(HEARTBEAT)
