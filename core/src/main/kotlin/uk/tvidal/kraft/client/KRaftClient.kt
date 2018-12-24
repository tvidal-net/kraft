package uk.tvidal.kraft.client

import uk.tvidal.kraft.RaftNode

interface KRaftClient {

    val self: RaftNode
}
