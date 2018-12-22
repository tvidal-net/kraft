package net.tvidal.kraft.message

import net.tvidal.kraft.domain.RaftNode

interface Message {

    val type: MessageType
    val from: RaftNode

    object EMPTY : Message {
        override val type = MessageType.NONE
        override val from = RaftNode.EMPTY
    }
}
