package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode

interface Message {

    val type: MessageType
    val from: RaftNode
}
