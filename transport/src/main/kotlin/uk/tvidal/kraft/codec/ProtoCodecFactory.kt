package uk.tvidal.kraft.codec

import uk.tvidal.kraft.codec.MessageCodec.binaryReader
import uk.tvidal.kraft.codec.MessageCodec.binaryWriter
import java.net.Socket

object ProtoCodecFactory : SocketCodecFactory {

    override fun reader(socket: Socket) = binaryReader(socket.getInputStream())

    override fun writer(socket: Socket) = binaryWriter(socket.getOutputStream())
}
