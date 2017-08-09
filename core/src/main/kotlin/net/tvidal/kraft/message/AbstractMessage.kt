package net.tvidal.kraft.message

import net.tvidal.kraft.domain.RaftNode

abstract class AbstractMessage(

  override val type: MessageType,
  override val from: RaftNode

) : Message {

}
