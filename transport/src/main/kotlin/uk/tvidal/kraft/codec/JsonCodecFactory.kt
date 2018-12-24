package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.MessageCodec.jsonReader
import uk.tvidal.kraft.codec.MessageCodec.jsonWriter
import uk.tvidal.kraft.transport.SocketMessageReader
import uk.tvidal.kraft.transport.SocketMessageWriter
import java.net.Socket

object JsonCodecFactory : SocketCodecFactory {

    override fun reader(socket: Socket): SocketMessageReader = jsonReader(socket.getInputStream())

    override fun writer(socket: Socket): SocketMessageWriter = jsonWriter(socket.getOutputStream())::write
}
