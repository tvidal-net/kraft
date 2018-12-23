package uk.tvidal.kraft.message.raft

import uk.tvidal.kraft.message.MessageType
import kotlin.reflect.KClass

enum class RaftMessageType(
    override val messageType: KClass<out RaftMessage>
) : MessageType {

    APPEND(AppendMessage::class),
    APPEND_ACK(AppendAckMessage::class),

    REQUEST_VOTE(RequestVoteMessage::class),
    VOTE(VoteMessage::class);
}
