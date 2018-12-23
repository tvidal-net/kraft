package uk.tvidal.kraft.codec

import uk.tvidal.kraft.message.Message
import java.net.Socket

abstract class SocketMessageReader(val socket: Socket) : Iterable<Message?>
