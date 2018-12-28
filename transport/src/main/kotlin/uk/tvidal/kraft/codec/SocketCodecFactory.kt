package uk.tvidal.kraft.codec

import uk.tvidal.kraft.transport.MessageReader
import uk.tvidal.kraft.transport.MessageWriter
import java.net.Socket

interface SocketCodecFactory {

    fun reader(socket: Socket): MessageReader

    fun writer(socket: Socket): MessageWriter
}
