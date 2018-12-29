package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.MessageCodec.jsonReader
import uk.tvidal.kraft.codec.MessageCodec.jsonWriter
import uk.tvidal.kraft.transport.MessageReader
import uk.tvidal.kraft.transport.MessageWriter
import java.net.Socket

object JsonCodecFactory : SocketCodecFactory {

    override fun reader(socket: Socket): MessageReader = jsonReader(socket.getInputStream())

    override fun writer(socket: Socket): MessageWriter = jsonWriter(socket.getOutputStream())
}
