package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND_ACK
import java.util.UUID

data class ClientAppendAckMessage(

    override val from: RaftNode,
    val id: UUID

) : AbstractClientMessage(CLIENT_APPEND_ACK) {

    override fun text() = "id=$id"

    override fun toString() = super.toString()
}
