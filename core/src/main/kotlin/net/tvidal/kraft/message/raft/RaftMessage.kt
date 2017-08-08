package net.tvidal.kraft.message.raft

import net.tvidal.kraft.message.Message

interface RaftMessage : Message {

    override val type: RaftMessageType

    val term: Long

}
