package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CONSUMER_ACK

data class ConsumerAckMessage(
    override val from: RaftNode,
    val index: Long
) : AbstractClientMessage(CONSUMER_ACK) {

    override fun text() = "index=$index"

    override fun toString() = super.toString()
}
