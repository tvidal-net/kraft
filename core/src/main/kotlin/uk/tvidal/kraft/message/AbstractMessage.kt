package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode

abstract class AbstractMessage(

    override val type: MessageType,
    final override val from: RaftNode

) : Message
