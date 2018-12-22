package uk.tvidal.kraft.message

import uk.tvidal.kraft.domain.RaftNode

interface Message {

    val type: MessageType
    val from: RaftNode

    object EMPTY : Message {
        override val type = MessageType.NONE
        override val from = RaftNode.EMPTY
    }
}
