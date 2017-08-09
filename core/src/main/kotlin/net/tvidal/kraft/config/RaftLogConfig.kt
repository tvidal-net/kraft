package net.tvidal.kraft.config

import net.tvidal.kraft.storage.RaftEntry
import net.tvidal.kraft.storage.RaftLog

interface RaftLogConfig {

    fun create(): RaftLog<RaftEntry>

}
