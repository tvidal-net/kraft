package net.tvidal.kraft.message.client

import net.tvidal.kraft.domain.RaftNode
import net.tvidal.kraft.message.client.ClientMessageType.*
import net.tvidal.kraft.storage.RaftEntry
import net.tvidal.kraft.storage.RaftEntryBatch

class ClientAppendMessage(

  override val from: RaftNode,

  val entries: RaftEntryBatch<RaftEntry>

) : AbstractClientMessage(CLIENT_APPEND, from)
