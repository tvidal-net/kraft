package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND
import uk.tvidal.kraft.storage.KRaftEntries
import java.util.UUID

class ClientAppendMessage(

    from: RaftNode,

    val data: KRaftEntries,
    var relay: RaftNode? = null,
    val id: UUID? = null

) : AbstractClientMessage(CLIENT_APPEND, from) {

    override fun text() = "id=$id relay=$relay data=$data"
}
