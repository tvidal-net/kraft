package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode

interface Message {

    val type: MessageType
    val from: RaftNode

    companion object {
        val EMPTY = object : Message {
            override val type = MessageType.NONE
            override val from = RaftNode.EMPTY
        }
    }
}
