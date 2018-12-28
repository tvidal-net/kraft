package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CONSUMER_REGISTER

data class ConsumerRegisterMessage(
    override val from: RaftNode,
    val fromIndex: Long,
    val maxBytes: Int = 4096
) : AbstractClientMessage(CONSUMER_REGISTER) {

    override fun text() = "fromIndex=$fromIndex maxBytes=$maxBytes"
}
