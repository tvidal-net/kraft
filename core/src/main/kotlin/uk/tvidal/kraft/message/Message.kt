package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode
import uk.tvidal.kraft.message.MessageType.NONE

interface Message {

    val type: MessageType
    val from: RaftNode

    object EMPTY : Message {
        override val type = NONE
        override val from = RaftNode.EMPTY
    }
}
