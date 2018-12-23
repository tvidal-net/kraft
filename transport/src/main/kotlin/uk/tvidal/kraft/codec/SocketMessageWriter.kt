package uk.tvidal.kraft.codec

import uk.tvidal.kraft.message.Message
import java.net.Socket

abstract class SocketMessageWriter(val socket: Socket) {

    abstract fun write(message: Message)
}
