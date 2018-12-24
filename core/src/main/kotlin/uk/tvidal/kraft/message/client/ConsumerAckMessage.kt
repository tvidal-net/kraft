package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CONSUMER_ACK

class ConsumerAckMessage(
    from: RaftNode,
    val index: Long
) : AbstractClientMessage(CONSUMER_ACK, from) {

    override fun toString() = "${super.toString()} index=$index"
}
