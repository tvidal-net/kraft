package net.tvidal.kraft.config

import net.tvidal.kraft.storage.KRaftEntry
import net.tvidal.kraft.storage.KRaftLog

interface LogConfig {

    fun create(): KRaftLog<KRaftEntry>

}
