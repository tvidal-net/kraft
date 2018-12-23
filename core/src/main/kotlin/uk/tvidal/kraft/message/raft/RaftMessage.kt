package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.message.Message

interface RaftMessage : Message {

    override val type: RaftMessageType

    val term: Long
}
