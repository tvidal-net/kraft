package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.DataMessage
import uk.tvidal.kraft.message.Payload
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND
import uk.tvidal.kraft.storage.KRaftEntries
import java.util.UUID

class ClientAppendMessage(

    from: RaftNode,

    @Payload
    override val data: KRaftEntries,

    var relay: RaftNode? = null,
    val id: UUID? = null

) : AbstractClientMessage(CLIENT_APPEND, from), DataMessage {

    override fun text() = "id=$id relay=$relay data=$data"
}
