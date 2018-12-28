package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CONSUMER_DATA
import uk.tvidal.kraft.storage.KRaftEntries

data class ConsumerDataMessage(
    override val from: RaftNode,
    val firstIndex: Long,
    val data: KRaftEntries
) : AbstractClientMessage(CONSUMER_DATA) {

    val lastIndex: Long
        get() = firstIndex + data.size - 1

    override fun text() = "from=$firstIndex $data"
}
