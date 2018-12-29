package uk.tvidal.kraft.message.transport

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.transport.TransportMessageType.HEARTBEAT
import java.lang.System.currentTimeMillis

data class HeartBeatMessage(
    override val from: RaftNode,
    val time: Long = currentTimeMillis(),
    val ping: Boolean = true
) : AbstractTransportMessage(HEARTBEAT)
