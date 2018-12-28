package uk.tvidal.kraft.message.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.client.ClientMessageType.CLIENT_ERROR

data class ClientErrorMessage(
    override val from: RaftNode,
    val error: ClientErrorType
) : AbstractClientMessage(CLIENT_ERROR) {

    override fun text() = error
}
