package uk.tvidal.kraft.codec

import java.net.Socket

interface SocketCodecFactory {

    fun reader(socket: Socket): SocketMessageReader

    fun writer(socket: Socket): SocketMessageWriter
}
