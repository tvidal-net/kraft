package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CONSUMER_REGISTER

class ConsumerRegisterMessage(
    from: RaftNode,
    val fromIndex: Long,
    val maxBytes: Int = 4096
) : AbstractClientMessage(CONSUMER_REGISTER, from) {

    override fun text() = "fromIndex=$fromIndex maxBytes=$maxBytes"
}
