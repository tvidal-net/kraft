package uk.tvidal.kraft.codec

import uk.tvidal.kraft.transport.SocketMessageReader
import uk.tvidal.kraft.transport.SocketMessageWriter
import java.net.Socket

interface SocketCodecFactory {

    fun reader(socket: Socket): SocketMessageReader

    fun writer(socket: Socket): SocketMessageWriter
}
