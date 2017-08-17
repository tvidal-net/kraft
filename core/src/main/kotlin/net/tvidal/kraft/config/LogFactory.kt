package net.tvidal.kraft.config

import net.tvidal.kraft.storage.KRaftLog

interface LogFactory {

    fun create(): KRaftLog

}
