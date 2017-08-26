package net.tvidal.kraft.config

import net.tvidal.kraft.storage.KRaftLog

interface KRaftLogFactory {

    fun create(): KRaftLog

}
