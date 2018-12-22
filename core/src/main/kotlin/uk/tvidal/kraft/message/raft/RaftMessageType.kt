package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.message.MessageType

enum class RaftMessageType : MessageType {

    APPEND,
    APPEND_ACK,

    REQUEST_VOTE,
    VOTE;
}
