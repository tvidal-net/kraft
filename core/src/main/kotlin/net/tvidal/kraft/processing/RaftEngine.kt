package net.tvidal.kraft.processing

import net.tvidal.kraft.domain.RaftEntry
import net.tvidal.kraft.domain.RaftState
import net.tvidal.kraft.storage.RaftStorage
import net.tvidal.kraft.transport.RaftTransport

interface RaftEngine {

    val state: RaftState

    val transport: RaftTransport

    val storage: RaftStorage<RaftEntry>

}
