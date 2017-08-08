package net.tvidal.kraft.message.raft

import net.tvidal.kraft.message.MessageType

enum class RaftMessageType : MessageType {

    APPEND,

    APPEND_ACK,

    REQUEST_VOTE,

    VOTE

}
