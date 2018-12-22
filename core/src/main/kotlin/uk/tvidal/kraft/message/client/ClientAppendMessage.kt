package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_APPEND
import uk.tvidal.kraft.storage.KRaftEntries

class ClientAppendMessage(

    from: RaftNode,

    val data: KRaftEntries

) : AbstractClientMessage(CLIENT_APPEND, from)
