package net.tvidal.kraft.config

import net.tvidal.kraft.domain.RaftNode

interface TransportNodeConfig {

    val node: RaftNode

}
