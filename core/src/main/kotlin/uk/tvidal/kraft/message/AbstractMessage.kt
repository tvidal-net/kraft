package uk.tvidal.kraft.message

import uk.tvidal.kraft.RaftNode

abstract class AbstractMessage(

    @Transient
    override val type: MessageType,
    final override val from: RaftNode

) : Message
