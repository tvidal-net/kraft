package net.tvidal.kraft.config

import net.tvidal.kraft.transport.RaftTransport

interface TransportConfig<out T : TransportNodeConfig> {

    val nodes: List<T>

    fun create(): RaftTransport

}
