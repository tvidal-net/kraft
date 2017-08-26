package net.tvidal.kraft.config

import net.tvidal.kraft.transport.KRaftTransport

interface KRaftTransportFactory {

    fun create(): KRaftTransport

}
