package net.tvidal.kraft.config

import net.tvidal.kraft.transport.KRaftTransport

interface TransportConfig<out T : TransportNodeConfig> {

    val nodes: List<T>

    fun create(): KRaftTransport

}
