package uk.tvidal.kraft.client

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.transport.MessageSender

abstract class AbstractKRaftClient(protected val server: MessageSender) : KRaftClient {

    override val self: RaftNode
        get() = server.self
}
