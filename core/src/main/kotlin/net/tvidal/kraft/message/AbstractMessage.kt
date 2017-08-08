package net.tvidal.kraft.message

import net.tvidal.kraft.domain.RaftNode

internal abstract class AbstractMessage(

  override val type: MessageType,
  override val source: RaftNode

) : Message {

}
