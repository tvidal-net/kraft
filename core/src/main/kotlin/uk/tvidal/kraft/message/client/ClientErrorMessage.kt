package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_ERROR

class ClientErrorMessage(
    from: RaftNode,
    val error: ClientErrorType
) : AbstractClientMessage(CLIENT_ERROR, from) {

    override fun text() = error
}
