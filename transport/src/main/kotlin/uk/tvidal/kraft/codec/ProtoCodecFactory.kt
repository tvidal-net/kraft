package uk.tvidal.kraft.codec

import uk.tvidal.kraft.transport.SocketMessageReader
import uk.tvidal.kraft.transport.SocketMessageWriter
import java.net.Socket

object ProtoCodecFactory : SocketCodecFactory {

    override fun reader(socket: Socket): SocketMessageReader {
        TODO("not implemented")
    }

    override fun writer(socket: Socket): SocketMessageWriter {
        TODO("not implemented")
    }
}
