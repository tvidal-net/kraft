package net.tvidal.kraft.message.client

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.client.ClientMessageType.*
import net.tvidal.kraft.storage.KRaftEntryBatch

class ClientAppendMessage(

  override val from: RaftNode,

  val entries: KRaftEntryBatch

) : AbstractClientMessage(CLIENT_APPEND, from)
