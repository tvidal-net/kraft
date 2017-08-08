package net.tvidal.kraft.message

import net.tvidal.kraft.domain.RaftNode

interface Message {

    val type: MessageType

    val source: RaftNode

}
