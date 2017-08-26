package net.tvidal.kraft.transport

import net.tvidal.kraft.config.KRaftTransportFactory

class DisruptorTransportFactory : KRaftTransportFactory {

    override fun create() = DisruptorTransport()

}
