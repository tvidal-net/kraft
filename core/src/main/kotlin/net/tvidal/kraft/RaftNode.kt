package net.tvidal.kraft

import net.tvidal.kraft.config.RaftConfig
import net.tvidal.kraft.processing.RaftEngine

interface RaftNode {

    val config: RaftConfig

    val engine: RaftEngine

}
