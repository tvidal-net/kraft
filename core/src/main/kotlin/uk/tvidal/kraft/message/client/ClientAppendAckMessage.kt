package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND_ACK
import java.util.UUID

data class ClientAppendAckMessage(

    override val from: RaftNode,
    val id: UUID,
    val error: ClientErrorType?,
    val leader: RaftNode?,
    val range: LongRange?,
    val term: Long?,
    val relay: RaftNode?

) : AbstractClientMessage(CLIENT_APPEND_ACK) {

    override fun text() = "range=$range error=$error id=$id term=$term leader=$leader relay=$relay"

    override fun toString() = super.toString()
}
