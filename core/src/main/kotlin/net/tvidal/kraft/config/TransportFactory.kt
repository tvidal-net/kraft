package net.tvidal.kraft.config

import net.tvidal.kraft.transport.KRaftTransport

interface TransportFactory {

    fun create(): KRaftTransport

}
