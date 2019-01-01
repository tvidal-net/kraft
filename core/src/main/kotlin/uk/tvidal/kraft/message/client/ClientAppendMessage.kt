package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.DataMessage
import uk.tvidal.kraft.message.Payload
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND
import uk.tvidal.kraft.storage.KRaftEntries
import java.util.UUID

data class ClientAppendMessage(

    override val from: RaftNode,

    @Payload
    override val data: KRaftEntries,

    var relay: RaftNode? = null

) : AbstractClientMessage(CLIENT_APPEND), DataMessage {

    @Transient
    val id: UUID? = data.id

    override fun text() = "relay=$relay id=$id data=$data"

    override fun toString() = super.toString()
}
