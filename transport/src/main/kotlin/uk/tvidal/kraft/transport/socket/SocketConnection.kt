package uk.tvidal.kraft.transport.socket

import uk.tvidal.kraft.codec.SocketCodecFactory
import uk.tvidal.kraft.message.Message
import java.net.Socket

class SocketConnection(
    codec: SocketCodecFactory,
    private val socket: Socket
) : KRaftConnection {

    private val writer = codec.writer(socket)

    override val read: Iterable<Message> = codec.reader(socket)

    override fun write(message: Message) {
        writer.invoke(message)
    }

    override fun close() {
        socket.close()
    }

    override fun toString() = socket.toString()
}
