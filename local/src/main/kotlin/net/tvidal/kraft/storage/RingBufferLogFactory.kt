package net.tvidal.kraft.storage

import net.tvidal.kraft.config.KRaftLogFactory

class RingBufferLogFactory(val size: Int) : KRaftLogFactory {

    override fun create() = RingBufferLog(size)

}
